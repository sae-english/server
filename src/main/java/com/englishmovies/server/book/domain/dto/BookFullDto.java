package com.englishmovies.server.book.domain.dto;

import com.englishmovies.server.movies.domain.dto.ContentBlockDto;
import lombok.Value;

import java.util.List;

@Value
public class BookFullDto {
    Long id;
    String name;
    String contentKey;
    String author;
    Integer year;
    String description;
    String note;
    List<ContentBlockDto> content;
}
