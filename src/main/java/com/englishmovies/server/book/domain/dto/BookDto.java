package com.englishmovies.server.book.domain.dto;

import lombok.Value;

@Value
public class BookDto {
    Long id;
    String name;
    String contentKey;
    String author;
    Integer year;
    String description;
}
