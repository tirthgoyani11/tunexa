package com.xapps.media.xmusic.service.resume;

import java.io.Serializable;

public class ResumeSong implements Serializable {
    private static final long serialVersionUID = 1L;

    public long id;
    public long albumId;
    public long duration;
    public String album;
    public String title;
    public String artist;
    public String path;
    public String artworkUri;

    public ResumeSong(com.xapps.media.xmusic.models.Song song) {
        this(song.title, song.artist, song.path, song.getArtworkUri() == null ? null : song.getArtworkUri().toString());
        id = song.id;
        albumId = song.albumId;
        album = song.album;
        duration = song.duration;
    }

    public com.xapps.media.xmusic.models.Song toSong() {
        if (path.startsWith("https://")) {
            return com.xapps.media.xmusic.models.Song.youtube(path, title, artist, duration, artworkUri);
        }
        return new com.xapps.media.xmusic.models.Song(id, path, title, artist, album, albumId,
                artist, 0, 0, duration, 0, 0, "audio/*", 0);
    }

    public ResumeSong() {
    }

    public ResumeSong(String title, String artist, String path, String artworkUri) {
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.artworkUri = artworkUri;
    }
}