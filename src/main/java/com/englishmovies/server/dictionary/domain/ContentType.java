package com.englishmovies.server.dictionary.domain;

/**
 * Тип контента, к которому привязана запись словаря.
 * MOVIE, SERIES, BOOK, ALBUM — произведение (work). EPISODE — эпизод сериала. COMEDY — стендап-спешл.
 */
public enum ContentType {
    MOVIE,
    SERIES,
    EPISODE,
    BOOK,
    ALBUM,
    COMEDY
}
