package com.xapps.media.xmusic.service.manager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.ResolvingDataSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import com.xapps.media.xmusic.online.YoutubeDownloader;
import com.xapps.media.xmusic.online.YoutubeMusicRepository;
import android.os.Looper;

import androidx.annotation.OptIn;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.util.ExperimentalApi;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import com.xapps.media.xmusic.data.RuntimeData;
import com.xapps.media.xmusic.models.Song;
import com.xapps.media.xmusic.R;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ExoPlayerManager {

    private final Handler playerHandler;
    private ExoPlayer player;
    private List<MediaItem> mediaItems;
    private final Context context;
    private final Uri fallbackUri;

    @OptIn(markerClass = UnstableApi.class)
    @ExperimentalApi
    public ExoPlayerManager(Context context) {
        fallbackUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.placeholder);
        
        this.context = context;
        playerHandler = new Handler(Looper.getMainLooper());
        
        DefaultRenderersFactory renderers = new DefaultRenderersFactory(context)
                                                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
                                                .setEnableAudioFloatOutput(false);

        AudioAttributes attrs = new AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build();

        DefaultHttpDataSource.Factory http = new DefaultHttpDataSource.Factory()
                .setUserAgent(YoutubeDownloader.USER_AGENT)
                .setConnectTimeoutMs(15000).setReadTimeoutMs(20000);
        ResolvingDataSource.Factory source = new ResolvingDataSource.Factory(
                new DefaultDataSource.Factory(context, http), dataSpec -> {
                    if (!"youtube".equals(dataSpec.uri.getScheme())) return dataSpec;
                    String watchUrl = dataSpec.uri.getQueryParameter("url");
                    if (watchUrl == null) throw new java.io.IOException("Missing YouTube URL");
                    String audioUrl = YoutubeMusicRepository.getInstance().resolveAudio(watchUrl);
                    return dataSpec.withUri(Uri.parse(audioUrl));
                });
        player = new ExoPlayer.Builder(context, renderers)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(source))
                .setLooper(Looper.getMainLooper())
                .experimentalSetDynamicSchedulingEnabled(false)
                .setAudioAttributes(attrs, true)
                .build();
                
        context.registerReceiver(noisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
    }

    public ExoPlayer getPlayer() {
        return player;
    }
    
    public Handler getPlayerHandler() {
        return playerHandler;
    }
    
    public void destroy() {
        playerHandler.post(() -> {
            if (player != null) {
                player.release();
                player = null;
            }
        });
        context.unregisterReceiver(noisyReceiver);
    }
    
    private final BroadcastReceiver noisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
				playerHandler.post(() -> {
                    if (player.isPlaying()) {
                        player.pause();
                    }
				});
            }
        }
    };
    
    public void updateMediaItems() {
        mediaItems = createMediaItems(new ArrayList<>(RuntimeData.songs));
    }

    public static List<MediaItem> createMediaItems(List<Song> songs) {
        ArrayList<MediaItem> items = new ArrayList<>(songs.size());
        for (Song song : songs) {
            MediaMetadata metadata = new MediaMetadata.Builder()
                    .setTitle(song.title).setArtist(song.artist).setAlbumTitle(song.album)
                    .setArtworkUri(song.getArtworkUri()).build();
            items.add(new MediaItem.Builder().setMediaId(song.path)
                    .setMediaMetadata(metadata).setUri(song.getPlaybackUri()).build());
        }
        return items;
    }

    public List<MediaItem> getMediaItems() {
        return createMediaItems(new ArrayList<>(RuntimeData.songs));
    }
}
