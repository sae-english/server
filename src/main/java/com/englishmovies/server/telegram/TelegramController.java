package com.englishmovies.server.telegram;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API для отправки сообщений в твой Telegram-канал с сервера.
 */
@RestController
@RequestMapping("/api/telegram")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RequiredArgsConstructor
public class TelegramController {

    private final TelegramService telegramService;

    /**
     * Отправить сообщение в канал.
     * POST /api/telegram/send  body: { "text": "Текст сообщения" }
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> send(@Valid @RequestBody SendMessageRequest request) {
        if (request == null || request.text() == null || request.text().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("ok", false, "error", "text обязателен и не должен быть пустым"));
        }
        if (!telegramService.isEnabled()) {
            return ResponseEntity.unprocessableEntity()
                    .body(Map.of("ok", false, "error", "Telegram не настроен: укажи telegram.bot-token и telegram.chat-id"));
        }
        boolean sent = telegramService.sendMessage(request.text());
        return ResponseEntity.ok(Map.of("ok", sent));
    }

    /**
     * Проверить, настроен ли Telegram (без отправки).
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "enabled", telegramService.isEnabled()
        ));
    }

    public record SendMessageRequest(@NotBlank String text) {}
}
