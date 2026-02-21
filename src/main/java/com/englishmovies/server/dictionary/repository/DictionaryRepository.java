package com.englishmovies.server.dictionary.repository;

import com.englishmovies.server.dictionary.domain.entity.DictionaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DictionaryRepository extends JpaRepository<DictionaryEntity, Long> {

    Optional<DictionaryEntity> findFirstByOrderByIdAsc();

    /** Поиск по value: LIKE %query% без учёта регистра. */
    List<DictionaryEntity> findByValueContainingIgnoreCase(String query);
}
