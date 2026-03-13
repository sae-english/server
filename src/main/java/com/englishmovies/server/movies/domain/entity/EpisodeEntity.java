package com.englishmovies.server.movies.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Эпизод сериала. Контент (сценарий) — блоки в EpisodeContentEntity (one-to-many).
 */
@Entity
@Table(
    name = "episode",
    schema = "englishmovies",
    uniqueConstraints = @UniqueConstraint(columnNames = {"series_id", "season", "episode_number"})
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
    @JoinColumn(name = "series_id", nullable = false)
    private SeriesEntity series;

    private Integer season;

    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;

    @Column(name = "content_key", unique = true, length = 255)
    private String contentKey;

    @Column(name = "episode_title", length = 500)
    private String episodeTitle;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "credits", columnDefinition = "jsonb")
    private JsonNode credits;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "episode", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EpisodeContentEntity> contentBlocks = new ArrayList<>();

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
