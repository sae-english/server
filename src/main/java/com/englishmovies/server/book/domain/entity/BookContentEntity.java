package com.englishmovies.server.book.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_content", schema = "englishmovies",
       uniqueConstraints = @UniqueConstraint(columnNames = { "book_id", "block_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    @Column(name = "block_id", nullable = false, length = 255)
    private String blockId;

    @Column(name = "block_type", nullable = false, length = 20)
    private String blockType;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "position", nullable = false)
    private Integer position;
}
