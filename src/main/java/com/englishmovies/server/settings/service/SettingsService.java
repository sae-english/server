package com.englishmovies.server.settings.service;

import com.englishmovies.server.settings.domain.entity.AppSettingEntity;
import com.englishmovies.server.settings.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Чтение и запись настроек приложения (таблица app_setting).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {

    public static final String KEY_TELEGRAM_SENDING_ENABLED = "telegram_sending_enabled";

    private final AppSettingRepository appSettingRepository;

    /**
     * Включена ли отправка слов в Telegram по расписанию.
     */
    @Transactional(readOnly = true)
    public boolean isTelegramSendingEnabled() {
        return appSettingRepository.findByKey(KEY_TELEGRAM_SENDING_ENABLED)
                .map(e -> "true".equalsIgnoreCase(e.getValue()))
                .orElse(true);
    }

    /**
     * Включить или выключить отправку слов в Telegram.
     */
    @Transactional
    public void setTelegramSendingEnabled(boolean enabled) {
        AppSettingEntity entity = appSettingRepository.findByKey(KEY_TELEGRAM_SENDING_ENABLED)
                .orElseGet(() -> {
                    AppSettingEntity e = new AppSettingEntity();
                    e.setKey(KEY_TELEGRAM_SENDING_ENABLED);
                    return e;
                });
        entity.setValue(enabled ? "true" : "false");
        entity.setUpdatedAt(Instant.now());
        appSettingRepository.save(entity);
        log.info("Telegram sending {}", enabled ? "enabled" : "disabled");
    }
}
