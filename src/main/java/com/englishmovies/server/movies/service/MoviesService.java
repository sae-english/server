package com.englishmovies.server.movies.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.englishmovies.server.movies.domain.dto.ContentBlockDto;
import com.englishmovies.server.movies.domain.dto.CreditsDto;
import com.englishmovies.server.movies.domain.dto.EpisodeDto;
import com.englishmovies.server.movies.domain.dto.EpisodeListDto;
import com.englishmovies.server.movies.domain.dto.MovieContentPageDto;
import com.englishmovies.server.movies.domain.dto.MovieDto;
import com.englishmovies.server.movies.domain.entity.EpisodeEntity;
import com.englishmovies.server.movies.domain.entity.MovieContentEntity;
import com.englishmovies.server.movies.domain.entity.MovieEntity;
import com.englishmovies.server.movies.converter.ContentBlockMapper;
import com.englishmovies.server.movies.converter.EpisodeConverter;
import com.englishmovies.server.movies.converter.MovieConverter;
import com.englishmovies.server.movies.repository.EpisodeRepository;
import com.englishmovies.server.movies.repository.MovieContentRepository;
import com.englishmovies.server.movies.repository.MovieRepository;
import com.englishmovies.server.movies.repository.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MoviesService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final WorkRepository workRepository;
    private final EpisodeRepository episodeRepository;
    private final MovieRepository movieRepository;
    private final MovieContentRepository movieContentRepository;
    private final MovieConverter movieConverter;
    private final EpisodeConverter episodeConverter;

    /**
     * Одна страница контента (пагинация по курсору). afterBlockId == null — первая страница.
     * limit — размер страницы; nextCursor в ответе — block_id для запроса следующей страницы.
     */
    @Transactional(readOnly = true)
    public Optional<MovieContentPageDto> getMovieContentPage(Long movieId, String afterBlockId, int limit) {
        int size = Math.min(Math.max(1, limit), 200);
        List<MovieContentEntity> page;
        if (afterBlockId == null || afterBlockId.isBlank()) {
            page = movieContentRepository.findByMovieIdOrderByPosition(movieId, PageRequest.of(0, size));
        } else {
            var cursor = movieContentRepository.findByMovieIdAndBlockId(movieId, afterBlockId);
            if (cursor.isEmpty()) return Optional.empty();
            int afterPosition = cursor.get().getPosition();
            page = movieContentRepository.findByMovieIdAndPositionGreaterThanOrderByPosition(movieId, afterPosition, PageRequest.of(0, size));
        }
        if (page.isEmpty()) return Optional.empty();
        return Optional.of(toMovieContentPageDto(page, size));
    }

    @Transactional(readOnly = true)
    public List<MovieDto> getLimitedMovies(Long limit) {
        int size = limit > Integer.MAX_VALUE ? Integer.MAX_VALUE : limit.intValue();
        return movieRepository.findRandomMoviesWithWork(PageRequest.of(0, size)).stream()
            .map(movieConverter::toDto)
            .toList();
    }

    /** Список эпизодов только для сериалов. Для фильмов возвращается пустой список (контент — через getMovieByTitleId). */
    @Transactional(readOnly = true)
    public List<EpisodeListDto> getEpisodesByTitleId(Long titleId) {
        return episodeRepository.findByWorkIdOrderBySeasonAscEpisodeNumberAsc(titleId).stream()
            .map(episodeConverter::toListDto)
            .toList();
    }

    /** Контент фильма по work id. Для сериалов не используется. */
    @Transactional(readOnly = true)
    public Optional<EpisodeDto> getMovieByTitleId(Long titleId) {
        return movieRepository.findByWorkIdWithWorkAndContent(titleId)
            .map(this::toEpisodeDtoFromMovie);
    }

    @Transactional(readOnly = true)
    public Optional<EpisodeDto> getEpisodeById(Long id) {
        return episodeRepository.findByIdWithWorkAndContent(id)
            .map(this::toEpisodeDto);
    }

    private EpisodeDto toEpisodeDto(EpisodeEntity entity) {
        var work = entity.getWork();
        var c = entity.getContent();
        return new EpisodeDto(
            entity.getId(),
            work.getId(),
            work.getName(),
            work.getType(),
            entity.getSeason(),
            entity.getEpisodeNumber(),
            entity.getEpisodeTitle(),
            entity.getContentKey(),
            c != null ? toJsonValue(c.getContent()) : null,
            c != null ? toJsonValue(c.getCredits()) : null,
            c != null ? c.getNote() : null
        );
    }

    private EpisodeDto toEpisodeDtoFromMovie(MovieEntity movie) {
        var work = movie.getWork();
        var contentBlocks = movie.getContentBlocks();
        Object contentJson = contentBlocks == null || contentBlocks.isEmpty() ? null : toJsonValue(assembleBlocksFromEntities(contentBlocks));
        return new EpisodeDto(
            movie.getId(),
            work.getId(),
            work.getName(),
            work.getType(),
            null,
            1,
            work.getName(),
            work.getContentKey(),
            contentJson,
            movie.getCredits() != null ? toJsonValue(movie.getCredits()) : null,
            movie.getNote()
        );
    }

    private MovieContentPageDto toMovieContentPageDto(List<MovieContentEntity> page, int requestedSize) {
        MovieContentEntity first = page.get(0);
        var meta = metadataFromFirst(first);
        List<ContentBlockDto> content = page.stream().map(ContentBlockMapper::fromEntity).toList();
        boolean hasMore = page.size() >= requestedSize;
        String nextCursor = hasMore ? page.get(page.size() - 1).getBlockId() : null;
        return new MovieContentPageDto(
            meta.movieId(),
            meta.contentKey(),
            meta.credits(),
            meta.note(),
            content,
            nextCursor,
            hasMore
        );
    }

    /** Метаданные контента фильма из первого блока (movie, work → contentKey, credits, note). */
    private record MovieContentMeta(Long movieId, String contentKey, CreditsDto credits, String note) {}

    private MovieContentMeta metadataFromFirst(MovieContentEntity first) {
        var movie = first.getMovie();
        var work = movie != null ? movie.getWork() : null;
        String contentKey = work != null ? work.getContentKey() : null;
        return new MovieContentMeta(
            movie != null ? movie.getId() : null,
            contentKey,
            movie != null ? toCreditsDto(movie.getCredits()) : null,
            movie != null ? movie.getNote() : null
        );
    }

    private JsonNode assembleBlocksFromEntities(List<MovieContentEntity> entities) {
        com.fasterxml.jackson.databind.node.ArrayNode array = OBJECT_MAPPER.createArrayNode();
        for (MovieContentEntity e : entities) {
            array.add(OBJECT_MAPPER.valueToTree(ContentBlockMapper.fromEntity(e)));
        }
        return array;
    }

    private CreditsDto toCreditsDto(JsonNode node) {
        if (node == null || node.isNull() || !node.isObject()) return null;
        return OBJECT_MAPPER.convertValue(node, CreditsDto.class);
    }

    /**
     * Converts JsonNode to plain Object (List/Map) for correct JSON serialization to UI.
     */
    private Object toJsonValue(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isArray()) return OBJECT_MAPPER.convertValue(node, List.class);
        if (node.isObject()) return OBJECT_MAPPER.convertValue(node, Map.class);
        return node.asText();
    }
}
