package com.englishmovies.server.comedy.domain.dto;

import lombok.Value;

@Value
public class ComedySpecialDto {
    Long id;
    String name;
    String contentKey;
    String performer;
    Integer year;
    String description;
}
