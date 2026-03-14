package com.englishmovies.server.dictionary.domain;

/**
 * Язык записи в словаре.
 */
public enum Language {
    ENGLISH("English", "🇬🇧");

    private final String displayName;
    private final String emoji;

    Language(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }
}
