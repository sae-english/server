package com.englishmovies.server.telegram;

import com.englishmovies.server.dictionary.domain.Language;
import com.englishmovies.server.dictionary.domain.dto.DictionaryDto;
import com.englishmovies.server.dictionary.service.DictionaryService;
import com.englishmovies.server.settings.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Отправка записей словаря в Telegram: выбор следующей записи, форматирование, отправка, отметка об отправке.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DictionaryTelegramService {

    private final DictionaryService dictionaryService;
    private final TelegramService telegramService;
    private final SettingsService settingsService;

    /**
     * Отправить в Telegram следующую запись словаря (по очереди last_sent_at).
     *
     * @return true, если сообщение отправлено; false, если отправка выключена в настройках, Telegram выключен, словарь пуст или ошибка
     */
    public boolean sendNextEntry() {
        if (!settingsService.isTelegramSendingEnabled()) {
            log.debug("Отправка в Telegram выключена в настройках, пропуск");
            return false;
        }
        if (!telegramService.isEnabled()) {
            log.debug("Telegram отключён (нет bot-token/chat-id), пропуск отправки");
            return false;
        }
        Optional<DictionaryDto> next = dictionaryService.findNextForTelegram();
        if (next.isEmpty()) {
            log.info("Словарь пуст, пропуск отправки в Telegram");
            return false;
        }
        DictionaryDto d = next.get();
        String value = d.getValue() != null ? d.getValue().strip() : "";
        String translation = d.getTranslation() != null ? d.getTranslation().strip() : "—";
        String comment = d.getComment() != null && !d.getComment().isBlank() ? d.getComment().strip() : null;
        String languageLabel = formatLanguage(d.getLanguage());

        String sourceTitle = dictionaryService.findExpandedById(d.getId())
            .map(exp -> exp.getTitle())
            .filter(t -> t != null && !t.isBlank())
            .orElse(null);

        String message = formatDictionaryMessage(value, translation, comment, languageLabel, sourceTitle);
        log.info("Отправка в Telegram: {} — {}", value, translation);
        boolean sent = telegramService.sendMessage(message, "HTML");
        if (sent && d.getId() != null) {
            dictionaryService.markSent(d.getId(), Instant.now());
        }
        return sent;
    }

    /** Экранирование для HTML (Telegram). */
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String formatLanguage(Language language) {
        if (language == null) return "🌐 —";
        return language.getEmoji() + " " + language.getDisplayName();
    }

    /** Стильное сообщение для Telegram: язык, слово, перевод, опционально комментарий. */
    private static String formatDictionaryMessage(String value, String translation, String comment, String languageLabel, String sourceTitle) {
        String v = escapeHtml(value);
        String t = escapeHtml(translation);
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(escapeHtml(languageLabel)).append("</b>\n");
        if (sourceTitle != null && !sourceTitle.isBlank()) {
            sb.append("📖 ").append(escapeHtml(sourceTitle)).append("\n");
        }
        sb.append("┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄\n\n");
        sb.append("<b>").append(v).append("</b>\n");
        sb.append("<i>").append(t).append("</i>\n");
        if (comment != null && !comment.isBlank()) {
            sb.append("\n┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄\n");
            sb.append("💬 ").append(escapeHtml(comment));
        }
        return sb.toString();
    }
}
