package com.ssafy.backend.mysql.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "track")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Track extends Common{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "track_id", nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "sound_url", nullable = false)
    private String soundUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "visibility", nullable = false)
    private Boolean visibility = false;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;

    @Column(name = "blocked", nullable = false)
    private Boolean blocked = false;

}