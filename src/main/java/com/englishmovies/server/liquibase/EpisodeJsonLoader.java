package com.englishmovies.server.liquibase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Universal loader: reads meta.json + content.json from classpath and returns episode data.
 * Path is relative to db/changelog/movies/ (e.g. "friends/episode-1", "interstellar").
 */
public final class EpisodeJsonLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String BASE = "db/changelog/movies/";

    private EpisodeJsonLoader() {
    }

    public static EpisodeData load(String relativePath) {
        String dir = BASE + relativePath + "/";
        JsonNode meta = readJson(dir + "meta.json");
        JsonNode content = readJson(dir + "content.json");

        if (meta == null) throw new IllegalStateException("meta.json not found: " + dir);
        if (content == null) throw new IllegalStateException("content.json not found: " + dir);

        Long titleId = meta.path("title_id").asLong();
        Integer season = meta.path("season").isNull() || meta.path("season").isMissingNode() ? null : meta.path("season").asInt();
        int episodeNumber = meta.path("episode_number").asInt();
        JsonNode episodeTitleNode = meta.path("episode_title");
        String episodeTitle = episodeTitleNode.isMissingNode() || episodeTitleNode.isNull() ? null : episodeTitleNode.asText();
        JsonNode creditsNode = meta.path("credits");
        String creditsJson = creditsNode.isMissingNode() || creditsNode.isNull() ? "{}" : creditsNode.toString();
        JsonNode noteNode = meta.path("note");
        String note = noteNode.isMissingNode() || noteNode.isNull() ? null : noteNode.asText();

        String contentJson = content.toString();

        return new EpisodeData(titleId, season, episodeNumber, episodeTitle, contentJson, creditsJson, note);
    }

    private static JsonNode readJson(String classpathPath) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathPath)) {
            if (is == null) return null;
            return MAPPER.readTree(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read " + classpathPath, e);
        }
    }

    public record EpisodeData(
        long titleId,
        Integer season,
        int episodeNumber,
        String episodeTitle,
        String contentJson,
        String creditsJson,
        String note
    ) {
    }
}
