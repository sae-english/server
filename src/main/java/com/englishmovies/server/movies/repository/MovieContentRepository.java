package com.englishmovies.server.movies.repository;

import com.englishmovies.server.movies.domain.entity.MovieContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieContentRepository extends JpaRepository<MovieContentEntity, Long> {
}
