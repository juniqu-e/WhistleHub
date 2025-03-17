package com.ssafy.backend.mysql.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "track")
public class Track {
    @Id
    @Column(name = "track_id", nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

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