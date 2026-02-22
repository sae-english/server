package com.englishmovies.server.movies.repository;

import com.englishmovies.server.movies.domain.entity.EpisodeContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EpisodeContentRepository extends JpaRepository<EpisodeContentEntity, Long> {

    List<EpisodeContentEntity> findByEpisodeIdOrderByPosition(Long episodeId);

    Optional<EpisodeContentEntity> findByEpisodeIdAndBlockId(Long episodeId, String blockId);
}
