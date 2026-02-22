package com.englishmovies.server.translate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Клиент Yandex Cloud Translate API v2.
 * Активен только при translate.provider=yandex. Иначе используется MyMemory (по умолчанию).
 * Настрой yandex.translate.api-key и yandex.translate.folder-id. См. YANDEX_TRANSLATE.md.
 */
@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "translate.provider", havingValue = "yandex")
@Slf4j
public class YandexTranslateService implements TranslateService {

    private static final String TRANSLATE_URL = "https://translate.api.cloud.yandex.net/translate/v2/translate";

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String folderId;

    public YandexTranslateService(RestTemplate restTemplate,
                                  @Value("${yandex.translate.api-key:}") String apiKey,
                                  @Value("${yandex.translate.folder-id:}") String folderId) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey != null ? apiKey.strip() : "";
        this.folderId = folderId != null ? folderId.strip() : "";
    }

    @Override
    public boolean isEnabled() {
        return !apiKey.isEmpty() && !folderId.isEmpty();
    }

    /**
     * Перевести текст на целевой язык (исходный язык определяется автоматически).
     *
     * @param text                 исходный текст
     * @param targetLanguageCode   код целевого языка (ISO 639-1, например "ru", "en")
     * @return переведённый текст или null при ошибке / отключённом сервисе
     */
    @Override
    public String translate(String text, String targetLanguageCode) {
        return translate(text, null, targetLanguageCode);
    }

    /**
     * Перевести текст с указанного языка на целевой.
     *
     * @param text                 исходный текст
     * @param sourceLanguageCode   код исходного языка (ISO 639-1) или null для автоопределения
     * @param targetLanguageCode   код целевого языка (ISO 639-1)
     * @return переведённый текст или null при ошибке / отключённом сервисе
     */
    @Override
    public String translate(String text, String sourceLanguageCode, String targetLanguageCode) {
        if (!isEnabled()) {
            log.debug("Yandex Translate не настроен (api-key или folder-id пусты)");
            return null;
        }
        if (text == null || text.isBlank()) {
            return text;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("folderId", folderId);
        body.put("texts", List.of(text));
        body.put("targetLanguageCode", targetLanguageCode);
        if (sourceLanguageCode != null && !sourceLanguageCode.isBlank()) {
            body.put("sourceLanguageCode", sourceLanguageCode);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + apiKey);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TranslateResponse> response = restTemplate.exchange(
                    TRANSLATE_URL,
                    HttpMethod.POST,
                    request,
                    TranslateResponse.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null
                    && response.getBody().translations() != null && !response.getBody().translations().isEmpty()) {
                return response.getBody().translations().get(0).text();
            }
            log.warn("Yandex Translate: неожиданный ответ {}", response.getStatusCode());
            return null;
        } catch (Exception e) {
            log.error("Ошибка вызова Yandex Translate: {}", e.getMessage());
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TranslateResponse(
            List<TranslationItem> translations
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TranslationItem(
            String text,
            @JsonProperty("detectedLanguageCode") String detectedLanguageCode
    ) {}
}
