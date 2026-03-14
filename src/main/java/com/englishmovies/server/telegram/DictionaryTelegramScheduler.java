package com.englishmovies.server.telegram;

import com.englishmovies.server.dictionary.domain.dto.DictionaryDto;
import com.englishmovies.server.dictionary.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Каждые 5 секунд берёт первую запись словаря (findFirst) и отправляет в Telegram: значение и перевод.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DictionaryTelegramScheduler {

    private final DictionaryService dictionaryService;
    private final TelegramService telegramService;

    @Scheduled(fixedRate = 60000) // раз в час
    public void sendFirstDictionaryEntry() {
        // Окно отправки по Москве: с 10:00 до 23:59 включительно
        ZonedDateTime nowMoscow = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        int hour = nowMoscow.getHour();
        // if (hour < 10 || hour > 23) {
        //     log.debug("Вне окна отправки ({} по Москве), пропуск", nowMoscow);
        //     return;
        // }

        if (!telegramService.isEnabled()) {
            log.debug("Telegram отключён (нет bot-token/chat-id), пропуск отправки");
            return;
        }
        Optional<DictionaryDto> next = dictionaryService.findNextForTelegram();
        if (next.isEmpty()) {
            log.info("Словарь пуст, пропуск отправки в Telegram");
            return;
        }
        DictionaryDto d = next.get();
        String value = d.getValue() != null ? d.getValue().strip() : "";
        String translation = d.getTranslation() != null ? d.getTranslation().strip() : "—";
        String comment = d.getComment() != null && !d.getComment().isBlank() ? d.getComment().strip() : null;

        String message = formatDictionaryMessage(value, translation, comment);
        log.info("Отправка в Telegram: {} — {}", value, translation);
        telegramService.sendMessage(message, "HTML");
        if (d.getId() != null) {
            dictionaryService.markSent(d.getId(), Instant.now());
        }
    }

    /** Экранирование для HTML (Telegram). */
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Собирает сообщение для Telegram: слово жирным, перевод и опционально комментарий. */
    private static String formatDictionaryMessage(String value, String translation, String comment) {
        String v = escapeHtml(value);
        String t = escapeHtml(translation);
        StringBuilder sb = new StringBuilder();
        sb.append("📚 <b>").append(v).append("</b>\n");
        sb.append("➜ ").append(t);
        if (comment != null && !comment.isBlank()) {
            sb.append("\n\n💬 ").append(escapeHtml(comment));
        }
        return sb.toString();
    }
}
