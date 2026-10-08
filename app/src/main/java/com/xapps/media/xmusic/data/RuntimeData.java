package com.xapps.media.xmusic.data;
import com.xapps.media.xmusic.models.Song;
import java.util.ArrayList;
import java.util.HashMap;

public class RuntimeData {
    public static long currentProgress = -1;
    public static volatile ArrayList<Song> localSongs = new ArrayList<>();
    public static volatile ArrayList<Song> songs = new ArrayList<>();
}
