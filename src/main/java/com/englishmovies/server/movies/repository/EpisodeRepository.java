package com.englishmovies.server.movies.repository;

import com.englishmovies.server.movies.domain.entity.EpisodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<EpisodeEntity, Long> {

    @Query("SELECT e FROM EpisodeEntity e JOIN FETCH e.series WHERE e.id = :id")
    Optional<EpisodeEntity> findByIdWithSeries(Long id);

    @Query("SELECT e FROM EpisodeEntity e LEFT JOIN FETCH e.series WHERE e.contentKey = :contentKey")
    Optional<EpisodeEntity> findByContentKeyWithSeries(String contentKey);

    @Query("SELECT e FROM EpisodeEntity e WHERE e.series.id = :seriesId ORDER BY e.season ASC NULLS FIRST, e.episodeNumber ASC")
    List<EpisodeEntity> findBySeriesIdOrderBySeasonAscEpisodeNumberAsc(Long seriesId);

    Optional<EpisodeEntity> findByContentKey(String contentKey);
}
