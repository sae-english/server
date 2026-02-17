package com.englishmovies.server.movies.repository;

import com.englishmovies.server.movies.domain.entity.Title;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TitleRepository extends JpaRepository<Title, Long> {
}
