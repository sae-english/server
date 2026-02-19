package com.englishmovies.server.movies.domain.dto;

import com.englishmovies.server.dictionary.domain.Language;

/**
 * Проекция для списка titles без загрузки content (тяжёлых JSON-полей).
 */
public interface TitleListProjection {
    Long getId();
    String getType();
    String getName();
    Language getLanguage();
    String getDirector();
    Integer getYear();
    String getDescription();
}
