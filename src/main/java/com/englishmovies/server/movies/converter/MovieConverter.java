package com.englishmovies.server.movies.converter;

import com.englishmovies.server.movies.domain.dto.MovieDto;
import com.englishmovies.server.movies.domain.entity.MovieEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Конвертер MovieEntity → MovieDto (MapStruct).
 */
@Mapper(componentModel = "spring")
public interface MovieConverter {

    @Mapping(target = "movieId", source = "id")
    MovieDto toDto(MovieEntity entity);
}
