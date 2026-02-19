package com.englishmovies.server.movies.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.englishmovies.server.movies.domain.dto.ContentBlockDto;
import com.englishmovies.server.movies.domain.dto.CreditsDto;
import com.englishmovies.server.movies.domain.dto.EpisodeDto;
import com.englishmovies.server.movies.domain.dto.EpisodeListDto;
import com.englishmovies.server.movies.domain.dto.MovieContentDto;
import com.englishmovies.server.movies.domain.dto.MovieDto;
import com.englishmovies.server.movies.domain.dto.TitleDto;
import com.englishmovies.server.movies.domain.dto.TitleListProjection;
import com.englishmovies.server.movies.domain.entity.EpisodeEntity;
import com.englishmovies.server.movies.domain.entity.MovieContentEntity;
import com.englishmovies.server.movies.domain.entity.MovieEntity;
import com.englishmovies.server.movies.domain.entity.WorkEntity;
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

    @Transactional(readOnly = true)
    public Optional<MovieContentDto> getMovieContentById(Long id) {
        return movieContentRepository.findById(id)
            .map(this::toMovieContentDto);
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
            .map(this::toEpisodeListDto)
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

    private TitleDto toTitleDto(TitleListProjection p) {
        return new TitleDto(
            p.getId(),
            p.getType(),
            p.getName(),
            p.getLanguage(),
            p.getDirector(),
            p.getYear(),
            p.getDescription()
        );
    }

    private TitleDto toTitleDto(WorkEntity work) {
        var movie = work.getMovie();
        var series = work.getSeries();
        String director = movie != null ? movie.getDirector() : (series != null ? series.getDirector() : null);
        Integer year = movie != null ? movie.getYear() : (series != null ? series.getYear() : null);
        String description = movie != null ? movie.getDescription() : (series != null ? series.getDescription() : null);
        return new TitleDto(
            work.getId(),
            work.getType(),
            work.getName(),
            work.getLanguage(),
            director,
            year,
            description
        );
    }

    private EpisodeListDto toEpisodeListDto(EpisodeEntity entity) {
        return new EpisodeListDto(
            entity.getId(),
            entity.getSeason(),
            entity.getEpisodeNumber(),
            entity.getEpisodeTitle()
        );
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
        var c = movie.getContent();
        return new EpisodeDto(
            movie.getId(),
            work.getId(),
            work.getName(),
            work.getType(),
            null,
            1,
            work.getName(),
            work.getContentKey(),
            c != null ? toJsonValue(c.getContent()) : null,
            c != null ? toJsonValue(c.getCredits()) : null,
            c != null ? c.getNote() : null
        );
    }

    private MovieContentDto toMovieContentDto(MovieContentEntity entity) {
        var work = entity.getMovie() != null ? entity.getMovie().getWork() : null;
        String contentKey = work != null ? work.getContentKey() : null;
        return new MovieContentDto(
            entity.getId(),
            entity.getMovie() != null ? entity.getMovie().getId() : null,
            contentKey,
            toContentBlocks(entity.getContent()),
            toCreditsDto(entity.getCredits()),
            entity.getNote()
        );
    }

    private List<ContentBlockDto> toContentBlocks(JsonNode node) {
        if (node == null || node.isNull() || !node.isArray()) return List.of();
        return OBJECT_MAPPER.convertValue(node, new TypeReference<>() {});
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
