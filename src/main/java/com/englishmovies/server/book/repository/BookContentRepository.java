package com.englishmovies.server.book.repository;

import com.englishmovies.server.book.domain.entity.BookContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookContentRepository extends JpaRepository<BookContentEntity, Long> {
    List<BookContentEntity> findByBookIdOrderByPosition(Long bookId);

    Optional<BookContentEntity> findByBookIdAndBlockId(Long bookId, String blockId);
}
