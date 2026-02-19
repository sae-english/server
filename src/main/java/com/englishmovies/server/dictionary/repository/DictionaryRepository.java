package com.englishmovies.server.dictionary.repository;

import com.englishmovies.server.dictionary.domain.entity.DictionaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DictionaryRepository extends JpaRepository<DictionaryEntity, Long> {

    Optional<DictionaryEntity> findFirstByOrderByIdAsc();
}
