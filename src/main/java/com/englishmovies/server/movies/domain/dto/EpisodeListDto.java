package com.englishmovies.server.movies.domain.dto;

import lombok.Value;

/**
 * DTO для списка эпизодов (без content)
 */
@Value
public class EpisodeListDto {
    Long id;
    Integer season;
    Integer episodeNumber;
    String episodeTitle;
}
