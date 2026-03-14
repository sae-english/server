package com.englishmovies.server.comedy.controller;

import com.englishmovies.server.comedy.domain.dto.ComedySpecialDto;
import com.englishmovies.server.comedy.domain.dto.ComedySpecialFullDto;
import com.englishmovies.server.comedy.service.ComedyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API стендап-спешлов: каталог и просмотр концерта с контентом.
 */
@RestController
@RequestMapping("/api/comedy")
@RequiredArgsConstructor
public class ComedyController {

    private final ComedyService comedyService;

    /**
     * Список спешлов для каталога. GET /api/comedy/specials?limit=N
     */
    @GetMapping("/specials")
    public ResponseEntity<List<ComedySpecialDto>> getSpecials(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(comedyService.getSpecials(limit));
    }

    /**
     * Один спешл с полным контентом (блоки по разделам). GET /api/comedy/specials/{id}
     */
    @GetMapping("/specials/{id}")
    public ResponseEntity<ComedySpecialFullDto> getSpecialById(@PathVariable Long id) {
        return comedyService.getSpecialById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
