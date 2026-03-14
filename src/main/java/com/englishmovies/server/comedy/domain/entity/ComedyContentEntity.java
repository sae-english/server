package com.englishmovies.server.comedy.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comedy_content", schema = "englishmovies",
       uniqueConstraints = @UniqueConstraint(columnNames = { "comedy_special_id", "block_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComedyContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comedy_special_id", nullable = false)
    private ComedySpecialEntity comedySpecial;

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
