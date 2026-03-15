package com.englishmovies.server.book.service;

import com.englishmovies.server.book.domain.dto.BookDto;
import com.englishmovies.server.book.domain.dto.BookFullDto;
import com.englishmovies.server.book.domain.entity.BookContentEntity;
import com.englishmovies.server.book.domain.entity.BookEntity;
import com.englishmovies.server.book.repository.BookContentRepository;
import com.englishmovies.server.book.repository.BookRepository;
import com.englishmovies.server.movies.domain.dto.ContentBlockDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookContentRepository bookContentRepository;

    @Transactional(readOnly = true)
    public List<BookDto> getBooks(int limit) {
        int size = Math.min(Math.max(1, limit), 100);
        return bookRepository.findRandomBooks(size).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<BookFullDto> getBookById(Long id) {
        return bookRepository.findById(id)
                .map(this::toFullDto);
    }

    private BookDto toDto(BookEntity e) {
        return new BookDto(
                e.getId(),
                e.getName(),
                e.getContentKey(),
                e.getAuthor(),
                e.getYear(),
                e.getDescription()
        );
    }

    private BookFullDto toFullDto(BookEntity e) {
        List<ContentBlockDto> blocks = bookContentRepository.findByBookIdOrderByPosition(e.getId()).stream()
                .map(this::toBlockDto)
                .toList();
        return new BookFullDto(
                e.getId(),
                e.getName(),
                e.getContentKey(),
                e.getAuthor(),
                e.getYear(),
                e.getDescription(),
                e.getNote(),
                blocks
        );
    }

    private ContentBlockDto toBlockDto(BookContentEntity b) {
        return new ContentBlockDto(
                b.getBlockType(),
                b.getBlockId(),
                b.getTitle(),
                b.getText(),
                null,
                null,
                null
        );
    }
}
