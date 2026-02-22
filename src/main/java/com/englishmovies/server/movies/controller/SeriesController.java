package com.englishmovies.server.movies.controller;

import com.englishmovies.server.movies.domain.dto.SeriesListDto;
import com.englishmovies.server.movies.service.SeriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API сериалов: список сериалов для каталога /series.
 */
@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    /**
     * Список сериалов (карточки). GET /api/series/titles?limit=N
     */
    @GetMapping("/titles")
    public ResponseEntity<List<SeriesListDto>> getLimitedSeries(@RequestParam Long limit) {
        List<SeriesListDto> list = seriesService.getLimitedSeries(limit);
        return ResponseEntity.ok(list);
    }
}
