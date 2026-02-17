package com.englishmovies.server.dictionary.domain.dto;

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
    String comment;
    Instant createdAt;
    Instant updatedAt;
}
