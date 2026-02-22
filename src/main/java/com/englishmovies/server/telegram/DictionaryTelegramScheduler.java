package com.englishmovies.server.telegram;

import com.englishmovies.server.dictionary.domain.dto.DictionaryDto;
import com.englishmovies.server.dictionary.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    // @Scheduled(fixedRate = 5000) // каждую минуту
    // public void sendFirstDictionaryEntry() {
    //     if (!telegramService.isEnabled()) {
    //         log.debug("Telegram отключён (нет bot-token/chat-id), пропуск отправки");
    //         return;
    //     }
    //     Optional<DictionaryDto> first = dictionaryService.findFirst();
    //     if (first.isEmpty()) {
    //         log.info("Словарь пуст, пропуск отправки в Telegram");
    //         return;
    //     }
    //     DictionaryDto d = first.get();
    //     String value = d.getValue() != null ? d.getValue() : "";
    //     String translation = d.getTranslation() != null ? d.getTranslation() : "—";
    //     String message = value + " — " + translation;
    //     log.info("Отправка в Telegram: {}", message);
    //     telegramService.sendMessage(message);
    // }
}
