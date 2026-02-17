package com.englishmovies.server.liquibase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * Liquibase CustomChange: truncates episode table, resets sequence, loads episodes from JSON.
 * runAlways="true" — runs on every app startup.
 */
public class EpisodeJsonLoaderChange implements CustomTaskChange {

    private static final String MANIFEST_PATH = "db/changelog/movies/episode-manifest.json";
    private static final String SCHEMA = "englishmovies";

    @Override
    public void execute(Database database) throws CustomChangeException {
        try {
            Connection conn = getConnection(database);
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                truncateAndReset(conn);
                loadEpisodes(conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        } catch (Exception e) {
            throw new CustomChangeException("Episode JSON load failed", e);
        }
    }

    private Connection getConnection(Database database) throws Exception {
        var dbConn = database.getConnection();
        if (!(dbConn instanceof JdbcConnection)) {
            throw new CustomChangeException("JdbcConnection required");
        }
        return ((JdbcConnection) dbConn).getWrappedConnection();
    }

    private void truncateAndReset(Connection conn) throws Exception {
        try (var stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE " + SCHEMA + ".episode RESTART IDENTITY CASCADE");
        }
    }

    private void loadEpisodes(Connection conn) throws Exception {
        List<String> paths = readManifest();
        String sql = "INSERT INTO " + SCHEMA + ".episode (title_id, season, episode_number, episode_title, content, credits, note) " +
            "VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?)";

        for (String path : paths) {
            EpisodeJsonLoader.EpisodeData data = EpisodeJsonLoader.load(path);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, data.titleId());
                ps.setObject(2, data.season());
                ps.setInt(3, data.episodeNumber());
                ps.setString(4, data.episodeTitle());
                ps.setString(5, data.contentJson());
                ps.setString(6, data.creditsJson());
                ps.setString(7, data.note());
                ps.executeUpdate();
            }
        }
    }

    private List<String> readManifest() throws Exception {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(MANIFEST_PATH)) {
            if (is == null) throw new IllegalStateException("Manifest not found: " + MANIFEST_PATH);
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new ObjectMapper().readValue(json, new TypeReference<List<String>>() {});
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Episode JSON loader executed";
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
    }

    @Override
    public void setUp() throws SetupException {
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}
