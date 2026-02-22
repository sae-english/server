package com.englishmovies.server.movies.domain.entity;

import com.englishmovies.server.movies.domain.ContentBlockType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Один блок контента эпизода (сценария). Одна запись = один элемент из content[].
 * По аналогии с MovieContentEntity.
 */
@Entity
@Table(name = "episode_content", schema = "englishmovies",
       uniqueConstraints = @UniqueConstraint(columnNames = { "episode_id", "block_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private EpisodeEntity episode;

    @Column(name = "block_id", nullable = false, length = 255)
    private String blockId;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type", nullable = false, length = 20)
    private ContentBlockType blockType;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "speaker", length = 500)
    private String speaker;

    @Column(name = "parenthetical", length = 500)
    private String parenthetical;

    @Column(name = "previous_id", length = 255)
    private String previousId;

    @Column(name = "next_id", length = 255)
    private String nextId;

    @Column(name = "position", nullable = false)
    private Integer position;
}
