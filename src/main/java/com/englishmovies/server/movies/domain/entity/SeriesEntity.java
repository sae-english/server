package com.englishmovies.server.movies.domain.entity;

import com.englishmovies.server.dictionary.domain.Language;
import jakarta.persistence.*;

import java.time.Instant;
import lombok.*;

/**
 * Сериал. Эпизоды — в EpisodeEntity.
 */
@Entity
@Table(name = "series", schema = "englishmovies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeriesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Language language = Language.ENGLISH;

    @Column(name = "content_key", unique = true, length = 255)
    private String contentKey;

    @Column(length = 255)
    private String director;

    private Integer year;

    @Column(columnDefinition = "TEXT")
    private String description;

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
