package com.englishmovies.server.movies.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Детали сериала. Одна запись на work с type SERIES.
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false, unique = true)
    private WorkEntity work;

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
