package com.englishmovies.server.movies.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Контент эпизода (сценарий). One-to-one с EpisodeEntity.
 */
@Entity
@Table(name = "episode_content", schema = "englishmovies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false, unique = true)
    private EpisodeEntity episode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode credits;

    @Column(columnDefinition = "TEXT")
    private String note;
}
