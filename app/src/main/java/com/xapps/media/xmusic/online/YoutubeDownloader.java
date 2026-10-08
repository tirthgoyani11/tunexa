package com.xapps.media.xmusic.online;

import androidx.annotation.NonNull;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class YoutubeDownloader extends Downloader {
    public static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/131.0.0.0 Mobile Safari/537.36";

    @NonNull
    @Override
    public Response execute(@NonNull Request request) throws IOException, ReCaptchaException {
        if (Thread.currentThread().isInterrupted()) throw new IOException("Request cancelled");
        HttpURLConnection connection = (HttpURLConnection) new URL(request.url()).openConnection();
        try {
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(20000);
            connection.setRequestMethod(request.httpMethod());
            connection.setRequestProperty("User-Agent", USER_AGENT);
            for (Map.Entry<String, List<String>> header : request.headers().entrySet()) {
                connection.setRequestProperty(header.getKey(), String.join(", ", header.getValue()));
            }
            byte[] body = request.dataToSend();
            if (body != null) {
                connection.setDoOutput(true);
                try (java.io.OutputStream output = connection.getOutputStream()) {
                    output.write(body);
                }
            }
            int code = connection.getResponseCode();
            if (code == 429) throw new ReCaptchaException("YouTube is temporarily limiting requests", request.url());
            InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String responseBody = "";
            if (stream != null) {
                try (InputStream input = stream; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        if (Thread.currentThread().isInterrupted()) throw new IOException("Request cancelled");
                        output.write(buffer, 0, read);
                        if (output.size() > 16 * 1024 * 1024) throw new IOException("Response too large");
                    }
                    responseBody = output.toString(StandardCharsets.UTF_8.name());
                }
            }
            return new Response(code, connection.getResponseMessage(), connection.getHeaderFields(),
                    responseBody, connection.getURL().toString());
        } finally {
            connection.disconnect();
        }
    }
}
