package com.englishmovies.server.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * По расписанию вызывает отправку следующей записи словаря в Telegram.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DictionaryTelegramScheduler {

    private final DictionaryTelegramService dictionaryTelegramService;

    @Scheduled(fixedRate = 60000) // раз в минуту (можно увеличить до часа — 3600000)
    public void sendNextDictionaryEntry() {
        // Окно по Москве: с 10:00 до 23:59 (раскомментируй при необходимости)
        // ZonedDateTime nowMoscow = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        // if (nowMoscow.getHour() < 10 || nowMoscow.getHour() > 23) return;

        dictionaryTelegramService.sendNextEntry();
    }
}
