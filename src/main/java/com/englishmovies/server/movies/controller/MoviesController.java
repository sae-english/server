package com.englishmovies.server.movies.controller;

import com.englishmovies.server.movies.domain.dto.EpisodeDto;
import com.englishmovies.server.movies.domain.dto.EpisodeListDto;
import com.englishmovies.server.movies.domain.dto.MovieContentPageDto;
import com.englishmovies.server.movies.domain.dto.MovieDto;
import com.englishmovies.server.movies.service.MoviesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API модуля movies: titles (фильмы), movie-content (контент фильма), episodes (эпизоды сериалов).
 */
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MoviesController {

    private final MoviesService moviesService;

    /**
     * Получить случайные фильмы (карточки для /movies). GET /api/movies/titles?limit=N
     */
    @GetMapping("/titles")
    public ResponseEntity<List<MovieDto>> getLimitedMovies(@RequestParam Long limit) {
        List<MovieDto> titles = moviesService.getLimitedMovies(limit);
        return ResponseEntity.ok(titles);
    }

    /**
     * Список эпизодов сериала по series id (titleId). GET /api/movies/titles/{titleId}/episodes
     */
    @GetMapping("/titles/{titleId}/episodes")
    public ResponseEntity<List<EpisodeListDto>> getEpisodesByTitleId(@PathVariable Long titleId) {
        List<EpisodeListDto> list = moviesService.getEpisodesByTitleId(titleId);
        return ResponseEntity.ok(list);
    }

    /**
     * Контент эпизода по id. GET /api/movies/episodes/{id}
     */
    @GetMapping("/episodes/{id}")
    public ResponseEntity<EpisodeDto> getEpisodeById(@PathVariable Long id) {
        return moviesService.getEpisodeById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Пагинация контента фильма по курсору: GET /movie-content/{movieId}/pages?after=&limit=100.
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
