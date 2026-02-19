package com.englishmovies.server.dictionary.domain.dto;

import com.englishmovies.server.dictionary.domain.ContentType;
import com.englishmovies.server.dictionary.domain.Language;
import lombok.Value;

import java.time.Instant;

/**
 * DTO записи словаря (ответ API).
 */
@Value
public class DictionaryDto {
    Long id;
    String value;
    String translation;
    Language language;
    String comment;
    String contentKey;
    ContentType contentType;
    String blockId;
    Instant createdAt;
    Instant updatedAt;
}
