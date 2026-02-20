package com.englishmovies.server.movies.controller;

import com.englishmovies.server.movies.domain.dto.MovieContentDto;
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
     * Получить контент фильма (сценарий) по id записи movies_content.
     */
    @GetMapping("/movie-content/{id}")
    public ResponseEntity<MovieContentDto> getMovieContentById(@PathVariable Long id) {
        return moviesService.getMovieContentById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
