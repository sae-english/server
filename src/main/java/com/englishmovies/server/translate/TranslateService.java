package com.englishmovies.server.translate;

/**
 * Общий контракт для сервиса перевода (MyMemory, Yandex и т.д.).
 */
public interface TranslateService {

    boolean isEnabled();

    /**
     * Перевести текст на целевой язык (исходный определяется автоматически, если поддерживается).
     */
    String translate(String text, String targetLanguageCode);

    /**
     * Перевести с указанного языка на целевой.
     * @param sourceLanguageCode null — автоопределение (если поддерживается).
     */
    String translate(String text, String sourceLanguageCode, String targetLanguageCode);
}
