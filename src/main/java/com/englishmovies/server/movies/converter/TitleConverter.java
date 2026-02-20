package com.englishmovies.server.movies.converter;

import com.englishmovies.server.movies.domain.dto.TitleDto;
import com.englishmovies.server.movies.domain.dto.TitleListProjection;
import com.englishmovies.server.movies.domain.entity.WorkEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Конвертер для TitleDto: из проекции (список) и из WorkEntity (детали).
 */
@Mapper(componentModel = "spring")
public interface TitleConverter {

    TitleDto toDto(TitleListProjection projection);

    @Mapping(target = "director", source = ".", qualifiedByName = "workDirector")
    @Mapping(target = "year", source = ".", qualifiedByName = "workYear")
    @Mapping(target = "description", source = ".", qualifiedByName = "workDescription")
    TitleDto toDto(WorkEntity work);

    @Named("workDirector")
    default String workDirector(WorkEntity work) {
        if (work.getMovie() != null) return work.getMovie().getDirector();
        if (work.getSeries() != null) return work.getSeries().getDirector();
        return null;
    }

    @Named("workYear")
    default Integer workYear(WorkEntity work) {
        if (work.getMovie() != null) return work.getMovie().getYear();
        if (work.getSeries() != null) return work.getSeries().getYear();
        return null;
    }

    @Named("workDescription")
    default String workDescription(WorkEntity work) {
        if (work.getMovie() != null) return work.getMovie().getDescription();
        if (work.getSeries() != null) return work.getSeries().getDescription();
        return null;
    }
}
