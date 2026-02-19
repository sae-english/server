package com.englishmovies.server.movies.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Эпизод сериала. Контент (сценарий) — в EpisodeContentEntity (one-to-one).
 */
@Entity
@Table(
    name = "episode",
    schema = "englishmovies",
    uniqueConstraints = @UniqueConstraint(columnNames = {"work_id", "season", "episode_number"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false)
    private WorkEntity work;

    private Integer season;

    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;

    @Column(name = "content_key", unique = true, length = 255)
    private String contentKey;

    @Column(name = "episode_title", length = 500)
    private String episodeTitle;

    @OneToOne(mappedBy = "episode", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private EpisodeContentEntity content;

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
