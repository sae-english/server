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

    @Scheduled(fixedRate = 300 000) // раз в час
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
        String value = d.getValue() != null ? d.getValue() : "";
        String translation = d.getTranslation() != null ? d.getTranslation() : "—";
        String message = value + " — " + translation;
        log.info("Отправка в Telegram: {}", message);
        telegramService.sendMessage(message);
        if (d.getId() != null) {
            dictionaryService.markSent(d.getId(), Instant.now());
        }
    }
}
