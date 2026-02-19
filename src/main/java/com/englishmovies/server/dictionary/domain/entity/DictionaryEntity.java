package com.englishmovies.server.dictionary.domain.entity;

import com.englishmovies.server.dictionary.domain.ContentType;
import com.englishmovies.server.dictionary.domain.Language;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Запись словаря: значение (слово/фраза), перевод, язык, комментарий.
 */
@Entity
@Table(name = "dictionary", schema = "englishmovies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String value;

    @Column(length = 500)
    private String translation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Language language = Language.ENGLISH;

    @Column(columnDefinition = "TEXT")
    private String comment;

    /** Ссылка на контент по стабильному ключу (work или episode: interstellar, friends-s01e05). */
    @Column(name = "content_key", length = 255)
    private String contentKey;

    /** Тип сущности: MOVIE, SERIES, EPISODE, BOOK, ALBUM. */
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 20)
    private ContentType contentType;

    /** id блока в content (guid диалога/сцены из content.json). */
    @Column(name = "block_id", length = 255)
    private String blockId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
