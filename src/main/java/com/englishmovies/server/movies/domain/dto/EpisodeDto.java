package com.englishmovies.server.movies.domain.dto;

import lombok.Value;

/**
 * DTO для эпизода/фильма (полный ответ с content).
 * content: List of blocks (scene, dialogue, action, transition, section).
 * credits: Map (writtenBy, directedBy, etc.).
 */
@Value
public class EpisodeDto {
    Long id;
    Long titleId;
    String titleName;
    String type;
    Integer season;
    Integer episodeNumber;
    String episodeTitle;
    Object content;
    Object credits;
    String note;
}
