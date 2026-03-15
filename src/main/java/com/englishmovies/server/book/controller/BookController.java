package com.englishmovies.server.book.controller;

import com.englishmovies.server.book.domain.dto.BookDto;
import com.englishmovies.server.book.domain.dto.BookFullDto;
import com.englishmovies.server.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API книг: каталог и просмотр книги с контентом (главы, абзацы).
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * Список книг для каталога. GET /api/books?limit=N
     */
    @GetMapping
    public ResponseEntity<List<BookDto>> getBooks(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(bookService.getBooks(limit));
    }

    /**
     * Одна книга с полным контентом (блоки: section = глава, text = абзац). GET /api/books/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookFullDto> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
