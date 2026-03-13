package com.englishmovies.server.liquibase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Liquibase CustomChange: loads movies, series and episode data from storage.
 * Reads storage/content-manifest.json — ключи movies и series_episode, в каждом массив путей.
 * Paths relative to storage. Storage: system property "storage.path" or "storage" relative to user.dir.
 * runAlways="true" — runs on every app startup.
 *
 * Movies: путь — до content.json; в файле work, film и content, credits, note. Контент хранится в таблице movies (эпизоды для фильмов не создаём).
 * Series_episode: путь — до папки сериала; в папке work.json и подпапки episode-1/, episode-2/ с content.json в каждой. Контент в таблице episode.
 */
public class StorageLoaderChange implements CustomTaskChange {

    private static final String MANIFEST_FILE = "content-manifest.json";
    private static final String WORK_FILE = "work.json";
    private static final String CONTENT_FILE = "content.json";
    private static final String SCHEMA = "englishmovies";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void execute(Database database) throws CustomChangeException {
        Path storageRoot = resolveStorageRoot();
        Path manifestPath = storageRoot.resolve(MANIFEST_FILE);
        if (!Files.isRegularFile(manifestPath)) {
            throw new CustomChangeException("Manifest not found: " + manifestPath);
        }

        try {
            String manifestJson = Files.readString(manifestPath, StandardCharsets.UTF_8);
            JsonNode manifest = MAPPER.readTree(manifestJson);

            Connection conn = getConnection(database);
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                truncateTables(conn);
                loadMovies(conn, storageRoot, manifest.path("movies"));
                loadSeriesEpisodes(conn, storageRoot, manifest.path("series_episode"));
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        } catch (CustomChangeException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomChangeException("Storage load failed", e);
        }
    }

    private static Path resolveStorageRoot() {
        String prop = System.getProperty("storage.path");
        if (prop != null && !prop.isBlank()) {
            return Paths.get(prop);
        }
        return Paths.get(System.getProperty("user.dir", ".")).resolve("storage");
    }

    private Connection getConnection(Database database) throws Exception {
        var dbConn = database.getConnection();
        if (!(dbConn instanceof JdbcConnection)) {
            throw new CustomChangeException("JdbcConnection required");
        }
        return ((JdbcConnection) dbConn).getWrappedConnection();
    }

    private void truncateTables(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE " + SCHEMA + ".episode_content RESTART IDENTITY CASCADE");
            stmt.execute("TRUNCATE TABLE " + SCHEMA + ".episode RESTART IDENTITY CASCADE");
            stmt.execute("TRUNCATE TABLE " + SCHEMA + ".movies_content RESTART IDENTITY CASCADE");
            stmt.execute("TRUNCATE TABLE " + SCHEMA + ".movies RESTART IDENTITY CASCADE");
            stmt.execute("TRUNCATE TABLE " + SCHEMA + ".series RESTART IDENTITY CASCADE");
        }
    }

    /** Movies: каждый элемент — путь к content.json. Метаданные в movies, контент чанками в movies_content. */
    private void loadMovies(Connection conn, Path storageRoot, JsonNode movies) throws Exception {
        if (!movies.isArray()) return;
        String moviesSql = "INSERT INTO " + SCHEMA + ".movies (name, language, content_key, director, year, description, credits, note) VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?)";
        String moviesContentSql = "INSERT INTO " + SCHEMA + ".movies_content (movie_id, block_id, block_type, title, text, description, speaker, parenthetical, previous_id, next_id, position) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        for (JsonNode pathNode : movies) {
            Path contentPath = storageRoot.resolve(pathNode.asText());
            JsonNode root = MAPPER.readTree(Files.readString(contentPath, StandardCharsets.UTF_8));
            JsonNode work = root.path("work");
            JsonNode film = root.path("film");
            if (work.isMissingNode() || film.isMissingNode()) {
                throw new CustomChangeException("movies content.json must contain 'work' and 'film': " + contentPath);
            }
            long movieId = insertMovie(conn, moviesSql, work, film, root);
            insertMovieContentBlocks(conn, moviesContentSql, movieId, root);
        }
    }

    /** Series_episode: каждый элемент — путь к папке сериала. Метаданные в series и episode, контент в episode_content. */
    private void loadSeriesEpisodes(Connection conn, Path storageRoot, JsonNode seriesEpisode) throws Exception {
        if (!seriesEpisode.isArray()) return;
        String seriesSql = "INSERT INTO " + SCHEMA + ".series (name, language, content_key, director, year, description) VALUES (?, ?, ?, ?, ?, ?)";
        String episodeSql = "INSERT INTO " + SCHEMA + ".episode (series_id, season, episode_number, episode_title, content_key, credits, note) VALUES (?, ?, ?, ?, ?, ?::jsonb, ?)";
        String episodeContentSql = "INSERT INTO " + SCHEMA + ".episode_content (episode_id, block_id, block_type, title, text, description, speaker, parenthetical, previous_id, next_id, position) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        for (JsonNode pathNode : seriesEpisode) {
            Path seriesDir = storageRoot.resolve(pathNode.asText());
            Path workPath = seriesDir.resolve(WORK_FILE);
            if (!Files.isRegularFile(workPath)) {
                throw new CustomChangeException("series folder must contain work.json: " + seriesDir);
            }
            JsonNode workFilm = MAPPER.readTree(Files.readString(workPath, StandardCharsets.UTF_8));
            JsonNode work = workFilm.path("work");
            JsonNode film = workFilm.path("film");
            if (work.isMissingNode() || film.isMissingNode()) {
                throw new CustomChangeException("work.json must contain 'work' and 'film': " + workPath);
            }
            long seriesId = insertSeries(conn, seriesSql, work, film);
            String workContentKey = textOrNull(work.path("contentKey"));
            if (workContentKey == null) workContentKey = textOrNull(work.path("content_key"));

            List<Path> episodeContentPaths = collectEpisodeContentPaths(seriesDir);
            for (Path epContent : episodeContentPaths) {
                try {
                    JsonNode epRoot = MAPPER.readTree(Files.readString(epContent, StandardCharsets.UTF_8));
                    long episodeId = insertEpisode(conn, episodeSql, epRoot, seriesId, workContentKey);
                    insertEpisodeContentBlocks(conn, episodeContentSql, episodeId, epRoot);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load " + epContent, e);
                }
            }
        }
    }

    /** Собирает пути к content.json эпизодов: либо season-N/episode-M, либо episode-N. */
    private List<Path> collectEpisodeContentPaths(Path seriesDir) throws Exception {
        List<Path> result = new ArrayList<>();
        List<Path> seasonDirs = new ArrayList<>();
        List<Path> flatEpisodeDirs = new ArrayList<>();
        try (Stream<Path> dirs = Files.list(seriesDir)) {
            dirs.filter(Files::isDirectory).forEach(p -> {
                String name = p.getFileName().toString();
                if (name.startsWith("season-")) seasonDirs.add(p);
                else if (name.startsWith("episode-")) flatEpisodeDirs.add(p);
            });
        }
        if (!seasonDirs.isEmpty()) {
            seasonDirs.sort(Comparator.comparing(p -> extractNumber(p.getFileName().toString(), "season-")));
            for (Path seasonDir : seasonDirs) {
                try (Stream<Path> epDirs = Files.list(seasonDir)) {
                    epDirs.filter(Files::isDirectory)
                        .filter(p -> p.getFileName().toString().startsWith("episode-"))
                        .sorted(Comparator.comparing(p -> extractNumber(p.getFileName().toString(), "episode-")))
                        .forEach(epDir -> {
                            Path content = epDir.resolve(CONTENT_FILE);
                            if (Files.isRegularFile(content)) result.add(content);
                        });
                }
            }
        } else {
            flatEpisodeDirs.sort(Comparator.comparing(p -> extractNumber(p.getFileName().toString(), "episode-")));
            for (Path epDir : flatEpisodeDirs) {
                Path content = epDir.resolve(CONTENT_FILE);
                if (Files.isRegularFile(content)) result.add(content);
            }
        }
        return result;
    }

    private static int extractNumber(String name, String prefix) {
        try {
            return Integer.parseInt(name.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        String s = node.asText(null);
        return (s != null && !s.isBlank()) ? s : null;
    }

    private long insertMovie(Connection conn, String sql, JsonNode work, JsonNode film, JsonNode root) throws Exception {
        String contentKey = textOrNull(work.path("contentKey"));
        if (contentKey == null) contentKey = textOrNull(work.path("content_key"));
        String name = work.path("name").asText();
        String language = work.path("language").asText("ENGLISH");
        JsonNode creditsNode = root.path("credits");
        String creditsJson = creditsNode.isMissingNode() || creditsNode.isNull() ? "{}" : creditsNode.toString();
        String note = root.path("note").asText(null);
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, language);
            ps.setString(3, contentKey);
            ps.setString(4, film.path("director").asText(null));
            ps.setObject(5, film.path("year").isMissingNode() || film.path("year").isNull() ? null : film.path("year").asInt());
            ps.setString(6, film.path("description").asText(null));
            ps.setString(7, creditsJson);
            ps.setString(8, note);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new IllegalStateException("Failed to get movie id");
    }

    private long insertSeries(Connection conn, String sql, JsonNode work, JsonNode film) throws Exception {
        String contentKey = textOrNull(work.path("contentKey"));
        if (contentKey == null) contentKey = textOrNull(work.path("content_key"));
        String name = work.path("name").asText();
        String language = work.path("language").asText("ENGLISH");
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, language);
            ps.setString(3, contentKey);
            ps.setString(4, film.path("director").asText(null));
            ps.setObject(5, film.path("year").isMissingNode() || film.path("year").isNull() ? null : film.path("year").asInt());
            ps.setString(6, film.path("description").asText(null));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new IllegalStateException("Failed to get series id");
    }

    private void insertMovieContentBlocks(Connection conn, String sql, long movieId, JsonNode root) throws Exception {
        JsonNode contentArray = root.path("content");
        if (!contentArray.isArray()) return;
        int size = contentArray.size();
        java.util.List<String> blockIds = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            JsonNode block = contentArray.get(i);
            String blockId = block.path("id").asText(null);
            if (blockId == null || blockId.isBlank()) {
                blockId = java.util.UUID.randomUUID().toString();
            }
            blockIds.add(blockId);
        }
        for (int i = 0; i < size; i++) {
            JsonNode block = contentArray.get(i);
            String blockId = blockIds.get(i);
            String blockType = block.path("type").asText("action");
            String title = block.path("title").asText(null);
            if (title != null && title.isEmpty()) title = null;
            String text = block.path("text").asText(null);
            if (text != null && text.isEmpty()) text = null;
            String description = block.path("description").asText(null);
            if (description != null && description.isEmpty()) description = null;
            String speaker = block.path("speaker").asText(null);
            if (speaker != null && speaker.isEmpty()) speaker = null;
            String parenthetical = block.path("parenthetical").asText(null);
            if (parenthetical != null && parenthetical.isEmpty()) parenthetical = null;
            String previousId = i > 0 ? blockIds.get(i - 1) : null;
            String nextId = i < size - 1 ? blockIds.get(i + 1) : null;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, movieId);
                ps.setString(2, blockId);
                ps.setString(3, blockType);
                ps.setString(4, title);
                ps.setString(5, text);
                ps.setString(6, description);
                ps.setString(7, speaker);
                ps.setString(8, parenthetical);
                ps.setString(9, previousId);
                ps.setString(10, nextId);
                ps.setInt(11, i);
                ps.executeUpdate();
            }
        }
    }

    private long insertEpisode(Connection conn, String sql, JsonNode contentJson, long seriesId, String workContentKey) throws Exception {
        Integer season = contentJson.path("season").isMissingNode() || contentJson.path("season").isNull()
            ? null : contentJson.path("season").asInt();
        int episodeNumber = contentJson.path("episode_number").asInt(1);
        String episodeTitle = contentJson.path("episode_title").asText(null);
        String episodeContentKey = textOrNull(contentJson.path("contentKey"));
        if (episodeContentKey == null) episodeContentKey = textOrNull(contentJson.path("content_key"));
        if (episodeContentKey == null && workContentKey != null && season != null) {
            episodeContentKey = workContentKey + "-s" + season + "e" + episodeNumber;
        }
        JsonNode creditsNode = contentJson.path("credits");
        String creditsJson = creditsNode.isMissingNode() || creditsNode.isNull() ? "{}" : creditsNode.toString();
        String note = contentJson.path("note").asText(null);
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, seriesId);
            ps.setObject(2, season);
            ps.setInt(3, episodeNumber);
            ps.setString(4, episodeTitle);
            ps.setString(5, episodeContentKey);
            ps.setString(6, creditsJson);
            ps.setString(7, note);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new IllegalStateException("Failed to get episode id");
    }

    /** По одному ряду на каждый блок content[] эпизода — по аналогии с movies_content. */
    private void insertEpisodeContentBlocks(Connection conn, String sql, long episodeId, JsonNode contentJson) throws Exception {
        JsonNode contentArray = contentJson.path("content");
        if (!contentArray.isArray()) return;
        int size = contentArray.size();
        java.util.List<String> blockIds = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            JsonNode block = contentArray.get(i);
            String blockId = block.path("id").asText(null);
            if (blockId == null || blockId.isBlank()) {
                blockId = java.util.UUID.randomUUID().toString();
            }
            blockIds.add(blockId);
        }
        for (int i = 0; i < size; i++) {
            JsonNode block = contentArray.get(i);
            String blockId = blockIds.get(i);
            String blockType = block.path("type").asText("action");
            String title = block.path("title").asText(null);
            if (title != null && title.isEmpty()) title = null;
            String text = block.path("text").asText(null);
            if (text != null && text.isEmpty()) text = null;
            String description = block.path("description").asText(null);
            if (description != null && description.isEmpty()) description = null;
            String speaker = block.path("speaker").asText(null);
            if (speaker != null && speaker.isEmpty()) speaker = null;
            String parenthetical = block.path("parenthetical").asText(null);
            if (parenthetical != null && parenthetical.isEmpty()) parenthetical = null;
            String previousId = i > 0 ? blockIds.get(i - 1) : null;
            String nextId = i < size - 1 ? blockIds.get(i + 1) : null;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, episodeId);
                ps.setString(2, blockId);
                ps.setString(3, blockType);
                ps.setString(4, title);
                ps.setString(5, text);
                ps.setString(6, description);
                ps.setString(7, speaker);
                ps.setString(8, parenthetical);
                ps.setString(9, previousId);
                ps.setString(10, nextId);
                ps.setInt(11, i);
                ps.executeUpdate();
            }
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Storage loader executed";
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
