package com.englishmovies.server.movies.converter;

import com.englishmovies.server.movies.domain.dto.ContentBlockDto;
import com.englishmovies.server.movies.domain.entity.MovieContentEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Конвертация блока контента в ContentBlockDto (тот же формат, что в GET /movie-content/.../pages).
 */
public final class ContentBlockMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ContentBlockMapper() {}

    /** Из JSON (эпизоды, словарь). */
    public static ContentBlockDto toDto(JsonNode node) {
        if (node == null || node.isNull()) {
            return new ContentBlockDto(null, null, null, null, null, null, null);
        }
        return OBJECT_MAPPER.convertValue(node, ContentBlockDto.class);
    }

    /** Из MovieContentEntity (фильмы — поля в таблице). */
    public static ContentBlockDto fromEntity(MovieContentEntity e) {
        if (e == null) {
            return new ContentBlockDto(null, null, null, null, null, null, null);
        }
        String type = e.getBlockType() != null ? e.getBlockType().name().toLowerCase() : null;
        return new ContentBlockDto(
            type,
            e.getBlockId(),
            e.getTitle(),
            e.getText(),
            e.getDescription(),
            e.getSpeaker(),
            e.getParenthetical()
        );
    }
}
