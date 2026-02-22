package com.englishmovies.server.translate;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API перевода (MyMemory по умолчанию). Для UI кнопки «Перевести».
 */
@RestController
@RequestMapping("/api/translate")
@RequiredArgsConstructor
public class TranslateController {

    private final TranslateService translateService;

    /**
     * POST /api/translate
     * Body: { "text": "Hello", "target": "ru" } (target по умолчанию "ru").
     * Ответ: { "translation": "Привет" } или 400/503 при ошибке.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> translate(@RequestBody Map<String, String> body) {
        String text = body != null ? body.get("text") : null;
        String target = body != null && body.containsKey("target") ? body.get("target") : "ru";
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (!translateService.isEnabled()) {
            return ResponseEntity.status(503).build();
        }
        String translation = translateService.translate(text.trim(), target);
        if (translation == null) {
            return ResponseEntity.status(503).build();
        }
        return ResponseEntity.ok(Map.of("translation", translation));
    }
}
