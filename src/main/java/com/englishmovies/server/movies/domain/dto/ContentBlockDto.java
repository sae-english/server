package com.englishmovies.server.movies.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Один блок сценария. type: section | action | scene | dialogue | transition.
 * Поля зависят от type: section → title; action/transition → text; scene → description; dialogue → speaker, text, parenthetical.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContentBlockDto(
    String type,
    String id,
    String title,
    String text,
    String description,
    String speaker,
    String parenthetical
) {}
