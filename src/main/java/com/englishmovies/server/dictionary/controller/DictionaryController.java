package com.englishmovies.server.dictionary.controller;

import com.englishmovies.server.dictionary.domain.dto.DictionaryDto;
import com.englishmovies.server.dictionary.domain.dto.DictionaryRequestDto;
import com.englishmovies.server.dictionary.domain.dto.ExpandedDictionaryDto;
import com.englishmovies.server.dictionary.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API словаря: сохранение, обновление, удаление и чтение всех слов.
 */
@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;

    /**
     * Создать новую запись в словаре.
     */
    @PostMapping
    public ResponseEntity<DictionaryDto> save(@RequestBody DictionaryRequestDto request) {
        DictionaryDto created = dictionaryService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновить запись по id.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DictionaryDto> update(@PathVariable Long id, @RequestBody DictionaryRequestDto request) {
        try {
            DictionaryDto updated = dictionaryService.update(id, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Удалить запись по id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            dictionaryService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Получить все записи словаря.
     */
    @GetMapping
    public ResponseEntity<List<DictionaryDto>> findAll() {
        List<DictionaryDto> list = dictionaryService.findAll();
        return ResponseEntity.ok(list);
    }

    /**
     * Поиск по value: LIKE %query% без учёта регистра. GET /api/dictionary/search?q=слово
     */
    @GetMapping("/search")
    public ResponseEntity<List<DictionaryDto>> search(@RequestParam(name = "q") String query) {
        List<DictionaryDto> list = dictionaryService.searchByValue(query);
        return ResponseEntity.ok(list);
    }

    /**
     * Поиск по value с расширением: каждая запись содержит dictionary, title (название фильма/сериала)
     * и block (тот же ContentBlockDto, что в GET /movie-content/.../pages).
     */
    @GetMapping("/search/expanded")
    public ResponseEntity<List<ExpandedDictionaryDto>> searchExpanded(@RequestParam(name = "q") String query) {
        List<ExpandedDictionaryDto> list = dictionaryService.searchByValueExpanded(query);
        return ResponseEntity.ok(list);
    }
}
