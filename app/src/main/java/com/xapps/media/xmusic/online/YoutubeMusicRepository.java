package com.xapps.media.xmusic.online;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.xapps.media.xmusic.models.Song;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.AudioTrackType;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class YoutubeMusicRepository {
    private static final YoutubeMusicRepository INSTANCE = new YoutubeMusicRepository();
    private final Map<String, CachedStream> streams = new LinkedHashMap<>();
    private final Map<String, ArrayList<Song>> categoryCache = new LinkedHashMap<>();

    @Nullable
    private static String getBestArtwork(@NonNull StreamInfoItem stream) {
        if (stream.getThumbnails() == null || stream.getThumbnails().isEmpty()) {
            return null;
        }
        var thumbnails = stream.getThumbnails();
        var best = thumbnails.get(thumbnails.size() - 1);
        String url = best.getUrl();
        if (url != null) {
            if (url.contains("/default.jpg")) {
                url = url.replace("/default.jpg", "/hqdefault.jpg");
            } else if (url.contains("/mqdefault.jpg")) {
                url = url.replace("/mqdefault.jpg", "/hqdefault.jpg");
            }
        }
        return url;
    }

    private YoutubeMusicRepository() {
        NewPipe.init(new YoutubeDownloader());
    }

    @NonNull
    public static YoutubeMusicRepository getInstance() { return INSTANCE; }

    public static final class SearchPage {
        public final ArrayList<Song> songs;
        @Nullable public final Page nextPage;

        public SearchPage(@NonNull ArrayList<Song> songs, @Nullable Page nextPage) {
            this.songs = songs;
            this.nextPage = nextPage;
        }
    }

    @NonNull
    public SearchPage search(@NonNull String query, @Nullable Page nextPage) throws Exception {
        SearchExtractor extractor = ServiceList.YouTube.getSearchExtractor(query.trim(),
                Collections.singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS), "");
        ListExtractor.InfoItemsPage<InfoItem> page;
        if (nextPage == null) {
            extractor.fetchPage();
            page = extractor.getInitialPage();
        } else {
            page = extractor.getPage(nextPage);
        }
        ArrayList<Song> songs = new ArrayList<>();
        for (InfoItem item : page.getItems()) {
            if (!(item instanceof StreamInfoItem)) continue;
            StreamInfoItem stream = (StreamInfoItem) item;
            if (stream.getDuration() <= 0) continue;
            String artwork = getBestArtwork(stream);
            songs.add(Song.youtube(stream.getUrl(), stream.getName(), stream.getUploaderName(),
                    stream.getDuration() * 1000L, artwork));
        }
        return new SearchPage(songs, Page.isValid(page.getNextPage()) ? page.getNextPage() : null);
    }

    @NonNull
    public ArrayList<Song> getTrendingMusic() throws Exception {
        synchronized (categoryCache) {
            ArrayList<Song> cached = categoryCache.get("trending");
            if (cached != null && !cached.isEmpty()) return new ArrayList<>(cached);
        }
        ArrayList<Song> songs = new ArrayList<>();
        try {
            KioskList kioskList = ServiceList.YouTube.getKioskList();
            KioskExtractor extractor = kioskList.getExtractorById("trending_music", null);
            extractor.fetchPage();
            for (Object item : extractor.getInitialPage().getItems()) {
                if (!(item instanceof StreamInfoItem)) continue;
                StreamInfoItem stream = (StreamInfoItem) item;
                if (stream.getDuration() <= 0) continue;
                String artwork = getBestArtwork(stream);
                songs.add(Song.youtube(stream.getUrl(), stream.getName(), stream.getUploaderName(),
                        stream.getDuration() * 1000L, artwork));
            }
        } catch (Exception e) {
            SearchPage fallback = search("Trending Songs Hits", null);
            songs = fallback.songs;
        }
        if (!songs.isEmpty()) {
            synchronized (categoryCache) {
                categoryCache.put("trending", new ArrayList<>(songs));
            }
        }
        return songs;
    }

    @NonNull
    public ArrayList<Song> getCategoryMusic(@NonNull String category) throws Exception {
        String key = category.trim().toLowerCase(java.util.Locale.ROOT);
        synchronized (categoryCache) {
            ArrayList<Song> cached = categoryCache.get(key);
            if (cached != null && !cached.isEmpty()) return new ArrayList<>(cached);
        }
        SearchPage page = search(category + " top songs", null);
        if (!page.songs.isEmpty()) {
            synchronized (categoryCache) {
                if (categoryCache.size() >= 20) categoryCache.remove(categoryCache.keySet().iterator().next());
                categoryCache.put(key, new ArrayList<>(page.songs));
            }
        }
        return page.songs;
    }

    // Called by ExoPlayer's loading thread. Persist watch URLs; signed audio URLs expire.
    @NonNull
    public String resolveAudio(@NonNull String watchUrl) throws IOException {
        synchronized (streams) {
            CachedStream cached = streams.get(watchUrl);
            if (cached != null && cached.expiresAt > System.currentTimeMillis()) return cached.url;
        }
        try {
            StreamExtractor extractor = ServiceList.YouTube.getStreamExtractor(watchUrl);
            extractor.fetchPage();
            AudioStream selected = null;
            for (AudioStream candidate : extractor.getAudioStreams()) {
                if (!candidate.isUrl() || candidate.getDeliveryMethod() != DeliveryMethod.PROGRESSIVE_HTTP) continue;
                if (candidate.getAudioTrackType() != null && candidate.getAudioTrackType() != AudioTrackType.ORIGINAL) continue;
                if (selected == null || candidate.getAverageBitrate() > selected.getAverageBitrate()) selected = candidate;
            }
            if (selected == null) throw new IOException("No playable audio is available for this track");
            String url = selected.getContent();
            if (!url.startsWith("https://")) throw new IOException("Invalid audio stream URL");
            synchronized (streams) {
                if (streams.size() >= 32) streams.remove(streams.keySet().iterator().next());
                streams.put(watchUrl, new CachedStream(url, System.currentTimeMillis() + 5 * 60 * 1000L));
            }
            return url;
        } catch (Exception error) {
            throw new IOException("Audio could not be loaded", error);
        }
    }

    public void invalidate(@NonNull String watchUrl) {
        synchronized (streams) { streams.remove(watchUrl); }
    }

    private static final class CachedStream {
        final String url;
        final long expiresAt;
        CachedStream(String url, long expiresAt) { this.url = url; this.expiresAt = expiresAt; }
    }
}
