package com.englishmovies.server.translate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Перевод через MyMemory API (бесплатно, по GET).
 * Лимит: 500 байт на запрос; 5000 символов/день без email, 50000 с параметром de=email.
 * См. https://mymemory.translated.net/doc/spec.php
 */
@Service
@Primary
@ConditionalOnProperty(name = "translate.provider", havingValue = "mymemory", matchIfMissing = true)
@Slf4j
public class MyMemoryTranslateService implements TranslateService {

    private static final String BASE_URL = "https://api.mymemory.translated.net/get";
    /** Максимум 500 байт (UTF-8) на один запрос. */
    private static final int MAX_QUERY_BYTES = 500;

    private final RestTemplate restTemplate;
    private final String email;

    public MyMemoryTranslateService(
            RestTemplate restTemplate,
            @Value("${translate.mymemory.email:}") String email) {
        this.restTemplate = restTemplate;
        this.email = email != null ? email.strip() : "";
    }

    @Override
    public boolean isEnabled() {
        return true; // API публичный, без ключа
    }

    @Override
    public String translate(String text, String targetLanguageCode) {
        return translate(text, null, targetLanguageCode);
    }

    @Override
    public String translate(String text, String sourceLanguageCode, String targetLanguageCode) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String langpair = buildLangPair(sourceLanguageCode, targetLanguageCode);
        if (langpair == null) {
            log.warn("MyMemory: не задана пара языков (target или source|target)");
            return null;
        }
        String truncated = truncateToMaxBytes(text, MAX_QUERY_BYTES);
        if (truncated.length() < text.length()) {
            log.debug("MyMemory: текст обрезан до {} байт", MAX_QUERY_BYTES);
        }

        // MyMemory expects langpair with literal | (e.g. en|ru); encoded %7C can cause "INVALID LANGUAGE PAIR"
        String url = buildGetUrl(BASE_URL, truncated, langpair);

        try {
            MyMemoryResponse response = restTemplate.getForObject(url, MyMemoryResponse.class);
            if (response != null && response.responseData() != null && response.responseData().translatedText() != null) {
                String translated = response.responseData().translatedText().strip();
                // MyMemory returns error messages in translatedText when langpair is invalid
                if (translated.toUpperCase().startsWith("INVALID LANGUAGE PAIR")) {
                    log.warn("MyMemory: invalid langpair (response: {})", translated);
                    return null;
                }
                return translated;
            }
            log.warn("MyMemory: пустой ответ");
            return null;
        } catch (Exception e) {
            log.error("Ошибка MyMemory Translate: {}", e.getMessage());
            return null;
        }
    }

    /** Builds langpair for MyMemory: 2-letter ISO (e.g. en|ru). Normalizes to lowercase and max 2 chars. */
    private static String buildLangPair(String source, String target) {
        if (target == null || target.isBlank()) return null;
        String t = normalizeLangCode(target);
        if (t == null) return null;
        if (source != null && !source.isBlank()) {
            String s = normalizeLangCode(source);
            if (s == null) return null;
            return s + "|" + t;
        }
        return "en|" + t;
    }

    /** Two-letter ISO 639-1 lowercase (e.g. "ru", "RU", "rus" -> "ru"). */
    private static String normalizeLangCode(String code) {
        if (code == null || code.isBlank()) return null;
        String c = code.strip().toLowerCase();
        if (c.length() >= 2) return c.substring(0, 2);
        return c.length() == 1 ? null : c; // single char not valid
    }

    private String buildGetUrl(String base, String q, String langpair) {
        String encodedQ = URLEncoder.encode(q, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(base).append("?q=").append(encodedQ).append("&langpair=").append(langpair);
        if (!email.isEmpty()) {
            sb.append("&de=").append(URLEncoder.encode(email, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private static String truncateToMaxBytes(String s, int maxBytes) {
        if (s == null) return null;
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) return s;
        int n = maxBytes;
        while (n > 0 && (bytes[n - 1] & 0xC0) == 0x80) n--;
        return new String(bytes, 0, n, StandardCharsets.UTF_8);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record MyMemoryResponse(@JsonProperty("responseData") ResponseData responseData) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ResponseData(@JsonProperty("translatedText") String translatedText) {}
}
