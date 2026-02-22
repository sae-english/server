package com.englishmovies.server.movies.domain.dto;

import lombok.Value;

/** DTO карточки сериала для каталога (список на /series). */
@Value
public class SeriesListDto {
    Long titleId;
    String name;
    String director;
    Integer year;
}
