package com.englishmovies.server.movies.repository;

import com.englishmovies.server.movies.domain.dto.TitleListProjection;
import com.englishmovies.server.movies.domain.entity.WorkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WorkRepository extends JpaRepository<WorkEntity, Long> {

    /** Список titles без загрузки content (только id, type, name, language, director, year, description). */
    @Query("SELECT w.id AS id, w.type AS type, w.name AS name, w.language AS language, " +
           "COALESCE(m.director, s.director) AS director, COALESCE(m.year, s.year) AS year, COALESCE(m.description, s.description) AS description " +
           "FROM WorkEntity w LEFT JOIN w.movie m LEFT JOIN w.series s ORDER BY w.id")
    List<TitleListProjection> findAllTitleList();

    @Query("SELECT w FROM WorkEntity w LEFT JOIN FETCH w.movie LEFT JOIN FETCH w.series ORDER BY w.id")
    List<WorkEntity> findAllWithMovieAndSeries();
}
