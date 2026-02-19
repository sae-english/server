package com.englishmovies.server.movies.domain.dto;

import lombok.Value;

import java.util.List;

/**
 * DTO контента фильма (сценарий): content, credits, note.
 * content: список блоков (scene, dialogue, action, transition, section).
 * credits: типизированные поля (writtenBy, storyBy, directedBy, source, scriptDate).
 */
@Value
public class MovieContentDto {
    Long id;
    Long movieId;
    /** Стабильный ключ контента (interstellar) для привязки в dictionary. */
    String contentKey;
    List<ContentBlockDto> content;
    CreditsDto credits;
    String note;
}
