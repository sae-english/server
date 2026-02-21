package com.englishmovies.server.movies.controller;

import com.englishmovies.server.movies.domain.dto.MovieContentPageDto;
import com.englishmovies.server.movies.domain.dto.MovieDto;
import com.englishmovies.server.movies.service.MoviesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API модуля movies: titles (фильмы/сериалы) и episodes (эпизоды/сценарии)
 */
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MoviesController {

    private final MoviesService moviesService;

    /**
     * Получить случайные фильмы (карточки для главной). Количество задаётся параметром limit.
     */
    @GetMapping("/titles")
    public ResponseEntity<List<MovieDto>> getLimitedMovies(@RequestParam Long limit) {
        List<MovieDto> titles = moviesService.getLimitedMovies(limit);
        return ResponseEntity.ok(titles);
    }

    /**
     * Пагинация контента по курсору: GET /movie-content/{movieId}/pages?after=&limit=100.
     * after — block_id последнего блока предыдущей страницы (пусто для первой). В ответе: content, nextCursor, hasMore.
     */
    @GetMapping("/movie-content/{movieId}/pages")
    public ResponseEntity<MovieContentPageDto> getMovieContentPage(
            @PathVariable Long movieId,
            @RequestParam(name = "after", required = false) String after,
            @RequestParam(name = "limit", defaultValue = "100") int limit) {
        return moviesService.getMovieContentPage(movieId, after, limit)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
