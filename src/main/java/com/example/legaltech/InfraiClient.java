package com.example.legaltech;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class InfraiClient {
    private final HttpClient http;
    private final String key;
    private final String baseUrl;

    public InfraiClient(String key, String baseUrl) {
        this.key = key;
        this.baseUrl = baseUrl;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public String flagValue(String flagKey, String userId) throws IOException, InterruptedException, InfraiError {
        String path = "/v1/flags/get_value/" + encode(flagKey) + "?user_id=" + encode(userId);
        return get(path);
    }

    public String user(String userId) throws IOException, InterruptedException, InfraiError {
        return get("/v1/auth/user/get/" + encode(userId));
    }

    private String get(String path) throws IOException, InterruptedException, InfraiError {
        int attempt = 0;
        while (true) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + key)
                    .header("Accept", "application/json")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            boolean ok = body.contains("\"ok\"\s*:\s*true") || body.contains("\"ok\":true");
            if (!ok) {
                if (response.statusCode() == 429 && attempt < 4) {
                    long retryAfter = retryAfterMillis(response);
                    Thread.sleep(retryAfter > 0 ? retryAfter : 250L * (1L << attempt));
                    attempt++;
                    continue;
                }
                String code = value(body, "code");
                String message = value(body, "message");
                throw new InfraiError(response.statusCode(), code, message.isEmpty() ? body : message);
            }
            if (response.statusCode() >= 400) {
                throw new InfraiError(response.statusCode(), "HTTP_ERROR", body);
            }
            return body;
        }
    }

    private static String value(String json, String field) {
        String marker = "\"" + field + "\"";
        int start = json.indexOf(marker);
        if (start < 0) return "";
        int colon = json.indexOf(':', start + marker.length());
        int quote = json.indexOf('"', colon + 1);
        if (colon < 0 || quote < 0) return "";
        int end = json.indexOf('"', quote + 1);
        return end < 0 ? "" : json.substring(quote + 1, end);
    }

    private static long retryAfterMillis(HttpResponse<String> response) {
        return response.headers().firstValue("Retry-After")
                .map(value -> {
                    try { return Long.parseLong(value.trim()) * 1000L; }
                    catch (NumberFormatException ignored) { return 0L; }
                }).orElse(0L);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
