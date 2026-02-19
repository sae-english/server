package com.englishmovies.server.movies.converter;

import com.englishmovies.server.movies.domain.dto.MovieDto;
import com.englishmovies.server.movies.domain.entity.MovieEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Конвертер MovieEntity → MovieDto (MapStruct).
 * У entity должен быть загружен work (например, через JOIN FETCH).
 */
@Mapper(componentModel = "spring")
public interface MovieConverter {

    @Mapping(target = "movieId", source = "id")
    @Mapping(target = "workId", source = "work.id")
    @Mapping(target = "type", source = "work.type")
    @Mapping(target = "name", source = "work.name")
    @Mapping(target = "language", source = "work.language")
    MovieDto toDto(MovieEntity entity);
}
