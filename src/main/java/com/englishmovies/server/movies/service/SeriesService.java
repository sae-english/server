package com.englishmovies.server.movies.service;

import com.englishmovies.server.movies.domain.dto.SeriesListDto;
import com.englishmovies.server.movies.domain.entity.SeriesEntity;
import com.englishmovies.server.movies.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;

    @Transactional(readOnly = true)
    public List<SeriesListDto> getLimitedSeries(long limit) {
        int size = limit > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) limit;
        List<SeriesEntity> random = seriesRepository.findRandomSeriesIds(size);
        if (random.isEmpty()) return List.of();
        List<Long> ids = random.stream().map(SeriesEntity::getId).toList();
        return seriesRepository.findByIdIn(ids).stream()
            .map(this::toListDto)
            .toList();
    }

    private SeriesListDto toListDto(SeriesEntity s) {
        return new SeriesListDto(
            s.getId(),
            s.getName(),
            s.getDirector(),
            s.getYear()
        );
    }
}
