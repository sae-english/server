package com.englishmovies.server.dictionary.domain.dto;

import com.englishmovies.server.dictionary.domain.ContentType;
import com.englishmovies.server.dictionary.domain.Language;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * Тело запроса для создания и обновления записи словаря.
 */
@Value
public class DictionaryRequestDto {
    String value;
    String translation;
    @NotNull(message = "language обязателен")
    Language language;
    String comment;
    /** Стабильный ключ контента (interstellar, friends-s01e05). */
    String contentKey;
    /** Тип сущности: MOVIE, SERIES, EPISODE, BOOK, ALBUM. */
    ContentType contentType;
    /** id блока в content (guid диалога/сцены из content.json). */
    String blockId;
}
