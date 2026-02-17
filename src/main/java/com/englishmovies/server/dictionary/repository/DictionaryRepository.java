package com.englishmovies.server.dictionary.repository;

import com.englishmovies.server.dictionary.domain.entity.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DictionaryRepository extends JpaRepository<Dictionary, Long> {
}
