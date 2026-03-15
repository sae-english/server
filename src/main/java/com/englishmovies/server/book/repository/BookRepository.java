package com.englishmovies.server.book.repository;

import com.englishmovies.server.book.domain.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<BookEntity, Long> {
    @Query(value = "SELECT * FROM englishmovies.books ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<BookEntity> findRandomBooks(int limit);

    Optional<BookEntity> findByContentKey(String contentKey);
}
