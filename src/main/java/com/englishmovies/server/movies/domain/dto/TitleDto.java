package com.englishmovies.server.movies.domain.dto;

import com.englishmovies.server.dictionary.domain.Language;
import lombok.Value;

/**
 * DTO для списка titles (фильмов и сериалов)
 */
@Value
public class TitleDto {
    Long id;
    String type;
    String name;
    Language language;
    String director;
    Integer year;
    String description;
}
