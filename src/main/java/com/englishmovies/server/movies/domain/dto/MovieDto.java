package com.englishmovies.server.movies.domain.dto;

import com.englishmovies.server.dictionary.domain.Language;
import lombok.Value;

/**
 * DTO карточки фильма (для списков, random и т.д.).
 */
@Value
public class MovieDto {
    Long movieId;
    Long workId;
    String type;
    String name;
    Language language;
    String director;
    Integer year;
    String description;
}
