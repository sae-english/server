package com.englishmovies.server.comedy.domain.dto;

import com.englishmovies.server.movies.domain.dto.ContentBlockDto;
import lombok.Value;
import java.util.List;

@Value
public class ComedySpecialFullDto {
    Long id;
    String name;
    String contentKey;
    String performer;
    Integer year;
    String description;
    String note;
    List<ContentBlockDto> content;
}
