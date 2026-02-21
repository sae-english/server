package com.englishmovies.server.dictionary.domain.dto;

import com.englishmovies.server.movies.domain.dto.ContentBlockDto;
import lombok.Value;

/**
 * Запись словаря с расширенными данными: название произведения и блок контента (тот же формат, что в GET /movie-content/.../pages).
 */
@Value
public class ExpandedDictionaryDto {
    DictionaryDto dictionary;
    /** Название фильма/сериала (work.name). */
    String title;
    /** Блок контента по block_id — тот же ContentBlockDto, что в пагинации контента фильма. */
    ContentBlockDto block;
}
