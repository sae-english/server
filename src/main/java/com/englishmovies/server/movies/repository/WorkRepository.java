package com.englishmovies.server.movies.repository;

import com.englishmovies.server.movies.domain.entity.WorkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WorkRepository extends JpaRepository<WorkEntity, Long> {

    Optional<WorkEntity> findByContentKey(String contentKey);

    @Query("SELECT w FROM WorkEntity w LEFT JOIN FETCH w.movie LEFT JOIN FETCH w.series ORDER BY w.id")
    List<WorkEntity> findAllWithMovieAndSeries();
}
