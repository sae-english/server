package com.englishmovies.server.movies.repository;

import com.englishmovies.server.movies.domain.entity.MovieContentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovieContentRepository extends JpaRepository<MovieContentEntity, Long> {

    List<MovieContentEntity> findByMovieIdOrderByPosition(Long movieId);

    List<MovieContentEntity> findByMovieIdOrderByPosition(Long movieId, Pageable pageable);

    /** Следующие блоки после position (для пагинации по курсору). */
    List<MovieContentEntity> findByMovieIdAndPositionGreaterThanOrderByPosition(Long movieId, Integer position, Pageable pageable);

    int countByMovieId(Long movieId);

    Optional<MovieContentEntity> findByMovieIdAndBlockId(Long movieId, String blockId);
}
