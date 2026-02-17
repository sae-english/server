package com.englishmovies.server.movies.domain.dto;

import lombok.Value;

/**
 * DTO для списка titles (фильмов и сериалов)
 */
@Value
public class TitleDto {
    Long id;
    String type;
    String name;
    String director;
    Integer year;
    String description;
}
