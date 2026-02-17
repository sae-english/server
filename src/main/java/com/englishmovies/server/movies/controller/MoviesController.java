package com.englishmovies.server.movies.controller;

import com.englishmovies.server.movies.domain.dto.EpisodeDto;
import com.englishmovies.server.movies.domain.dto.EpisodeListDto;
import com.englishmovies.server.movies.domain.dto.TitleDto;
import com.englishmovies.server.movies.service.MoviesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API модуля movies: titles (фильмы/сериалы) и episodes (эпизоды/сценарии)
 */
@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class MoviesController {

    private final MoviesService moviesService;

    public MoviesController(MoviesService moviesService) {
        this.moviesService = moviesService;
    }

    /**
     * Получить все titles (фильмы и сериалы)
     */
    @GetMapping("/titles")
    public ResponseEntity<List<TitleDto>> getAllTitles() {
        List<TitleDto> titles = moviesService.getAllTitles();
        return ResponseEntity.ok(titles);
    }

    /**
     * Получить список эпизодов для title (фильма или сериала)
     */
    @GetMapping("/titles/{titleId}/episodes")
    public ResponseEntity<List<EpisodeListDto>> getEpisodesByTitleId(@PathVariable Long titleId) {
        List<EpisodeListDto> episodes = moviesService.getEpisodesByTitleId(titleId);
        return ResponseEntity.ok(episodes);
    }

    /**
     * Получить эпизод или фильм по id (с полным content)
     */
    @GetMapping("/episodes/{id}")
    public ResponseEntity<EpisodeDto> getEpisodeById(@PathVariable Long id) {
        return moviesService.getEpisodeById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
