package com.xapps.media.xmusic.models;

import android.net.Uri;

import java.util.Locale;

public class Song {

    public final long id;
    public final String path;
    public final String title;
    public final String artist;
    public final String album;
    public final long albumId;
    public final String albumArtist;
    public final int year;
    public final int track;
    public final long duration;
    public final long dateAdded;
    public final long dateModified;
    public final String mimeType;
    public final long size;

    private String searchKey;
    private Uri artworkUri;

    public Song(
            long id,
            String path,
            String title,
            String artist,
            String album,
            long albumId,
            String albumArtist,
            int year,
            int track,
            long duration,
            long dateAdded,
            long dateModified,
            String mimeType,
            long size
    ) {
        this.id = id;
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.albumId = albumId;
        this.albumArtist = albumArtist;
        this.year = year;
        this.track = track;
        this.duration = duration;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.mimeType = mimeType;
        this.size = size;
    }

    public static Song youtube(String url, String title, String artist, long duration, String artwork) {
        if (artwork != null) {
            if (artwork.contains("/default.jpg")) {
                artwork = artwork.replace("/default.jpg", "/hqdefault.jpg");
            } else if (artwork.contains("/mqdefault.jpg")) {
                artwork = artwork.replace("/mqdefault.jpg", "/hqdefault.jpg");
            }
        }
        Song song = new Song(-1L - Integer.toUnsignedLong(url.hashCode()), url, title, artist,
                "Online", -1, artist, 0, 0, duration, 0, 0, "audio/*", 0);
        song.artworkUri = artwork == null ? null : Uri.parse(artwork);
        return song;
    }

    public boolean isOnline() {
        Uri uri = Uri.parse(path);
        String host = uri.getHost();
        return "https".equals(uri.getScheme()) && ("www.youtube.com".equals(host)
                || "youtube.com".equals(host) || "music.youtube.com".equals(host));
    }

    public Uri getPlaybackUri() {
        return isOnline() ? new Uri.Builder().scheme("youtube").authority("watch")
                .appendQueryParameter("url", path).build() : Uri.fromFile(new java.io.File(path));
    }

    public String getSearchKey() {
        if (searchKey == null) {
            searchKey = (
                    safe(title) + " " +
                    safe(artist) + " " +
                    safe(album) + " " +
                    safe(albumArtist)
            ).toLowerCase(Locale.ROOT);
        }
        return searchKey;
    }

    public Uri getArtworkUri() {
		try { 
            if (artworkUri == null && !isOnline()) {
                artworkUri = Uri.parse("content://media/external/audio/media/" + id + "/albumart");
            }
            return artworkUri;
		} catch (Exception e) {
			return null;
		}
    }

    public String getFormattedDuration() {
        long seconds = duration / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        if (hours > 0) {
            return String.format(
                    Locale.getDefault(),
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    remainingSeconds
            );
        }

        return String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                remainingSeconds
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}