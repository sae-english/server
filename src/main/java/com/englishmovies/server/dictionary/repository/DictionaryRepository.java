package com.englishmovies.server.dictionary.repository;

import com.englishmovies.server.dictionary.domain.entity.DictionaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DictionaryRepository extends JpaRepository<DictionaryEntity, Long> {

    Optional<DictionaryEntity> findFirstByOrderByIdAsc();

    /**
     * Запись для Telegram: сначала записи без lastSentAt, затем с минимальным lastSentAt.
     */
    @Query("""
        SELECT d
        FROM DictionaryEntity d
        ORDER BY
          CASE WHEN d.lastSentAt IS NULL THEN 0 ELSE 1 END,
          d.lastSentAt ASC
        """)
    Page<DictionaryEntity> findNextForTelegram(Pageable pageable);

    /** Поиск по value: LIKE %query% без учёта регистра. */
    List<DictionaryEntity> findByValueContainingIgnoreCase(String query);
}
