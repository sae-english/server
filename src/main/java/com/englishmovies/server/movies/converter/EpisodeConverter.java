package com.englishmovies.server.movies.converter;

import com.englishmovies.server.movies.domain.dto.EpisodeListDto;
import com.englishmovies.server.movies.domain.entity.EpisodeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EpisodeConverter {

    EpisodeListDto toListDto(EpisodeEntity entity);
}
