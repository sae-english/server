package com.englishmovies.server.movies.domain.entity;

import com.englishmovies.server.movies.domain.ContentBlockType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Один блок контента фильма (сценария). Одна запись = один элемент из content[].
 * Порядок: previous_id и next_id — block_id предыдущего/следующего блока; position — индекс для пагинации.
 * Поля блока разнесены по колонкам по типу: section → title; action/transition → text; scene → description; dialogue → speaker, text, parenthetical.
 */
@Entity
@Table(name = "movies_content", schema = "englishmovies",
       uniqueConstraints = @UniqueConstraint(columnNames = { "movie_id", "block_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private MovieEntity movie;

    /** Id блока (guid, например 7dd8f2d4-56f0-4c2a-a84b-22918fd05754). */
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

    /** block_id предыдущего блока в порядке контента; null для первого. */
    @Column(name = "previous_id", length = 255)
    private String previousId;

    /** block_id следующего блока; null для последнего. */
    @Column(name = "next_id", length = 255)
    private String nextId;

    @Column(name = "position", nullable = false)
    private Integer position;
}
