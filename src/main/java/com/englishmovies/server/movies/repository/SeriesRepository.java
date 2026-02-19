package com.englishmovies.server.movies.repository;

import com.englishmovies.server.movies.domain.entity.SeriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeriesRepository extends JpaRepository<SeriesEntity, Long> {

    Optional<SeriesEntity> findByWorkId(Long workId);
}
