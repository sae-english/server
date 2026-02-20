package com.englishmovies.server.dictionary.converter;

import com.englishmovies.server.dictionary.domain.dto.DictionaryDto;
import com.englishmovies.server.dictionary.domain.dto.DictionaryRequestDto;
import com.englishmovies.server.dictionary.domain.entity.DictionaryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DictionaryConverter {

    DictionaryDto toDto(DictionaryEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DictionaryEntity toEntity(DictionaryRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(DictionaryRequestDto dto, @MappingTarget DictionaryEntity entity);
}
