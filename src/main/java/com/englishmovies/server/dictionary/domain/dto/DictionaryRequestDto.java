package com.englishmovies.server.dictionary.domain.dto;

import lombok.Value;

/**
 * Тело запроса для создания и обновления записи словаря.
 */
@Value
public class DictionaryRequestDto {
    String value;
    String translation;
    String comment;
}
