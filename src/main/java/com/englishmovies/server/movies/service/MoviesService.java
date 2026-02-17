package com.englishmovies.server.movies.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.englishmovies.server.movies.domain.dto.EpisodeDto;
import com.englishmovies.server.movies.domain.dto.EpisodeListDto;
import com.englishmovies.server.movies.domain.dto.TitleDto;
import com.englishmovies.server.movies.domain.entity.Episode;
import com.englishmovies.server.movies.domain.entity.Title;
import com.englishmovies.server.movies.repository.EpisodeRepository;
import com.englishmovies.server.movies.repository.TitleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MoviesService {

    private final TitleRepository titleRepository;
    private final EpisodeRepository episodeRepository;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public MoviesService(TitleRepository titleRepository, EpisodeRepository episodeRepository) {
        this.titleRepository = titleRepository;
        this.episodeRepository = episodeRepository;
    }

    @Transactional(readOnly = true)
    public List<TitleDto> getAllTitles() {
        return titleRepository.findAll().stream()
            .map(this::toTitleDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<EpisodeListDto> getEpisodesByTitleId(Long titleId) {
        return episodeRepository.findByTitleIdOrderBySeasonAscEpisodeNumberAsc(titleId).stream()
            .map(this::toEpisodeListDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<EpisodeDto> getEpisodeById(Long id) {
        return episodeRepository.findByIdWithTitle(id)
            .map(this::toEpisodeDto);
    }

    private TitleDto toTitleDto(Title entity) {
        return new TitleDto(
            entity.getId(),
            entity.getType(),
            entity.getName(),
            entity.getDirector(),
            entity.getYear(),
            entity.getDescription()
        );
    }

    private EpisodeListDto toEpisodeListDto(Episode entity) {
        return new EpisodeListDto(
            entity.getId(),
            entity.getSeason(),
            entity.getEpisodeNumber(),
            entity.getEpisodeTitle()
        );
    }

    private EpisodeDto toEpisodeDto(Episode entity) {
        return new EpisodeDto(
            entity.getId(),
            entity.getTitle().getId(),
            entity.getTitle().getName(),
            entity.getTitle().getType(),
            entity.getSeason(),
            entity.getEpisodeNumber(),
            entity.getEpisodeTitle(),
            toJsonValue(entity.getContent()),
            toJsonValue(entity.getCredits()),
            entity.getNote()
        );
    }

    /**
     * Converts JsonNode to plain Object (List/Map) for correct JSON serialization to UI.
     * JsonNode otherwise serializes as its Java Bean structure.
     */
    private Object toJsonValue(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isArray()) return OBJECT_MAPPER.convertValue(node, List.class);
        if (node.isObject()) return OBJECT_MAPPER.convertValue(node, Map.class);
        return node.asText();
    }
}
