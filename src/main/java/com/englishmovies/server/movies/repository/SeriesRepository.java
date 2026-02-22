package com.englishmovies.server.movies.repository;

import com.englishmovies.server.movies.domain.entity.SeriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeriesRepository extends JpaRepository<SeriesEntity, Long> {

    Optional<SeriesEntity> findByWorkId(Long workId);

    @Query(value = "SELECT s.* FROM englishmovies.series s ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<SeriesEntity> findRandomSeriesIds(@Param("limit") int limit);

    @Query("SELECT s FROM SeriesEntity s JOIN FETCH s.work WHERE s.id IN :ids")
    List<SeriesEntity> findByIdInWithWork(@Param("ids") List<Long> ids);
}
