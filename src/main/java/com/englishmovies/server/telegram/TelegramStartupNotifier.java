package com.englishmovies.server.telegram;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramStartupNotifier {

    private final TelegramService telegramService;

//    @EventListener(ApplicationReadyEvent.class)
//    public void onApplicationReady() {
//        if (!telegramService.isEnabled()) {
//            log.info("Telegram не настроен — задай TELEGRAM_BOT_TOKEN (chat-id уже в application-dev.yaml)");
//            return;
//        }
//        boolean sent = telegramService.sendMessage("Server Started");
//        if (!sent) {
//            log.warn("Не удалось отправить «Server Started» в Telegram — смотри логи выше");
//        }
//    }
}
