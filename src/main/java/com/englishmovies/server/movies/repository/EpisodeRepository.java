package com.englishmovies.server.movies.repository;

import com.englishmovies.server.movies.domain.entity.EpisodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<EpisodeEntity, Long> {

    @Query("SELECT e FROM EpisodeEntity e JOIN FETCH e.work WHERE e.id = :id")
    Optional<EpisodeEntity> findByIdWithWork(Long id);

    @Query("SELECT e FROM EpisodeEntity e JOIN FETCH e.work LEFT JOIN FETCH e.content WHERE e.id = :id")
    Optional<EpisodeEntity> findByIdWithWorkAndContent(Long id);

    @Query("SELECT e FROM EpisodeEntity e WHERE e.work.id = :workId ORDER BY e.season ASC NULLS FIRST, e.episodeNumber ASC")
    List<EpisodeEntity> findByWorkIdOrderBySeasonAscEpisodeNumberAsc(Long workId);

    Optional<EpisodeEntity> findByContentKey(String contentKey);

    @Query("SELECT e FROM EpisodeEntity e LEFT JOIN FETCH e.work LEFT JOIN FETCH e.content WHERE e.contentKey = :contentKey")
    Optional<EpisodeEntity> findByContentKeyWithWorkAndContent(String contentKey);
}
