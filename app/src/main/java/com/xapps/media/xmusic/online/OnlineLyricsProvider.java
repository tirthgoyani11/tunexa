package com.xapps.media.xmusic.online;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class OnlineLyricsProvider {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static long currentRequestId = 0;

    public interface Callback {
        void onLyrics(String lyrics);
    }

    public static void fetch(String title, String artist, Callback callback) {
        final long requestId = ++currentRequestId;
        EXECUTOR.execute(() -> {
            String lyrics = fetchInternal(title, artist);
            MAIN.post(() -> {
                if (requestId == currentRequestId) {
                    callback.onLyrics(resultOrNull(lyrics));
                }
            });
        });
    }

    private static String fetchInternal(String title, String artist) {
        if (title == null || title.trim().isEmpty()) return null;
        try {
            StringBuilder urlBuilder = new StringBuilder("https://lrclib.net/api/get?");
            urlBuilder.append("track_name=").append(URLEncoder.encode(title.trim(), "UTF-8"));
            if (artist != null && !artist.trim().isEmpty() && !"Unknown Artist".equalsIgnoreCase(artist.trim())) {
                urlBuilder.append("&artist_name=").append(URLEncoder.encode(artist.trim(), "UTF-8"));
            }

            HttpURLConnection conn = (HttpURLConnection) new URL(urlBuilder.toString()).openConnection();
            conn.setRequestProperty("User-Agent", "Tunexa-Android/1.0");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            if (conn.getResponseCode() == 200) {
                try (InputStream in = conn.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                    JSONObject obj = new JSONObject(sb.toString());
                    if (obj.has("syncedLyrics") && !obj.isNull("syncedLyrics")) {
                        String synced = obj.getString("syncedLyrics").trim();
                        if (!synced.isEmpty()) return synced;
                    }
                    if (obj.has("plainLyrics") && !obj.isNull("plainLyrics")) {
                        String plain = obj.getString("plainLyrics").trim();
                        if (!plain.isEmpty()) return plain;
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String resultOrNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s;
    }
}
