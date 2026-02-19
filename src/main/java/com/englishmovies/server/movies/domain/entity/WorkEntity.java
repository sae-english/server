package com.englishmovies.server.movies.domain.entity;

import com.englishmovies.server.dictionary.domain.Language;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Универсальное произведение: фильм, сериал, книга, альбом и т.д.
 * Детали по типам — в отдельных таблицах (movies, series, book, album) со ссылкой work_id.
 */
@Entity
@Table(name = "work", schema = "englishmovies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(name = "content_key", unique = true, length = 255)
    private String contentKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Language language = Language.ENGLISH;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToOne(mappedBy = "work", fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = true)
    private MovieEntity movie;

    @OneToOne(mappedBy = "work", fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = true)
    private SeriesEntity series;

    @OneToMany(mappedBy = "work", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EpisodeEntity> episodes = new ArrayList<>();

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
