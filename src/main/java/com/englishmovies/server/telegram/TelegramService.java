package com.englishmovies.server.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Отправка сообщений в Telegram-канал.
 * Настрой telegram.bot-token и telegram.chat-id в конфиге (или TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID в окружении).
 */
@Service
@Slf4j
public class TelegramService {

    private static final String SEND_MESSAGE_URL = "https://api.telegram.org/bot%s/sendMessage";

    private final RestTemplate restTemplate;
    private final String botToken;
    private final String chatId;

    public TelegramService(RestTemplate restTemplate,
                           @Value("${telegram.bot-token:}") String botToken,
                           @Value("${telegram.chat-id:}") String chatId) {
        this.restTemplate = restTemplate;
        this.botToken = botToken != null ? botToken.strip() : "";
        this.chatId = chatId != null ? chatId.strip() : "";
    }

    public boolean isEnabled() {
        return !botToken.isEmpty() && !chatId.isEmpty();
    }

    /**
     * Отправить текстовое сообщение в канал.
     *
     * @param text текст сообщения
     * @return true, если отправлено успешно (или Telegram не настроен — не считаем ошибкой)
     */
    public boolean sendMessage(String text) {
        if (!isEnabled()) {
            log.info("Telegram не настроен (bot-token или chat-id пусты), сообщение не отправлено");
            return false;
        }
        if (text == null || text.isBlank()) {
            log.warn("Пустой текст сообщения в Telegram");
            return false;
        }

        String url = SEND_MESSAGE_URL.formatted(botToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var body = Map.of(
                "chat_id", chatId,
                "text", text
        );
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Сообщение отправлено в Telegram");
                return true;
            }
            log.warn("Telegram API вернул {}: {}", response.getStatusCode(), response.getBody());
            return false;
        } catch (Exception e) {
            log.error("Ошибка отправки в Telegram: {}", e.getMessage());
            return false;
        }
    }
}
