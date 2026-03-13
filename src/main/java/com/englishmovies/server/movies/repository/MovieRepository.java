package com.englishmovies.server.movies.repository;

import com.englishmovies.server.movies.domain.entity.MovieEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<MovieEntity, Long> {

    Optional<MovieEntity> findByContentKey(String contentKey);

    @Query("SELECT m FROM MovieEntity m ORDER BY RANDOM()")
    List<MovieEntity> findRandomMovies(Pageable pageable);

    @Query("SELECT DISTINCT m FROM MovieEntity m LEFT JOIN FETCH m.contentBlocks WHERE m.id = :movieId")
    Optional<MovieEntity> findByIdWithContent(Long movieId);
}
