package com.englishmovies.server.movies.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Типизированные кредиты сценария (по структуре Interstellar).
 * При необходимости можно расширять новыми полями (transcribedBy, additionalTranscribingBy и т.д.).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreditsDto(
    String writtenBy,
    String storyBy,
    String directedBy,
    String source,
    String scriptDate
) {}
