package com.englishmovies.server.movies.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Детали фильма. Одна запись на work с type MOVIE. Контент (сценарий) — в MovieContentEntity (one-to-one).
 */
@Entity
@Table(name = "movies", schema = "englishmovies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false, unique = true)
    private WorkEntity work;

    @Column(length = 255)
    private String director;

    private Integer year;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToOne(mappedBy = "movie", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private MovieContentEntity content;

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
