package com.englishmovies.server.movies.repository;

import com.englishmovies.server.movies.domain.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    @Query("SELECT e FROM Episode e JOIN FETCH e.title WHERE e.id = :id")
    Optional<Episode> findByIdWithTitle(Long id);

    @Query("SELECT e FROM Episode e WHERE e.title.id = :titleId ORDER BY e.season ASC NULLS FIRST, e.episodeNumber ASC")
    List<Episode> findByTitleIdOrderBySeasonAscEpisodeNumberAsc(Long titleId);
}
