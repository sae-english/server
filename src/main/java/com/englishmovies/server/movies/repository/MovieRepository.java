package com.englishmovies.server.movies.repository;

import com.englishmovies.server.movies.domain.entity.MovieEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<MovieEntity, Long> {

    Optional<MovieEntity> findByWorkId(Long workId);

    @Query(value = "SELECT * FROM englishmovies.movies ORDER BY RANDOM() LIMIT 5", nativeQuery = true)
    List<MovieEntity> findRandomMovies(Long limit);

    /** Случайные фильмы с подгруженным work (для конвертера в MovieDto). */
    @Query("SELECT m FROM MovieEntity m JOIN FETCH m.work ORDER BY RANDOM()")
    List<MovieEntity> findRandomMoviesWithWork(Pageable pageable);

    @Query("SELECT m FROM MovieEntity m JOIN FETCH m.work WHERE m.work.id = :workId")
    Optional<MovieEntity> findByWorkIdWithWork(Long workId);

    @Query("SELECT DISTINCT m FROM MovieEntity m JOIN FETCH m.work LEFT JOIN FETCH m.contentBlocks WHERE m.work.id = :workId")
    Optional<MovieEntity> findByWorkIdWithWorkAndContent(Long workId);
}
