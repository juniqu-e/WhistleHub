package com.ssafy.backend.mysql.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "`like`")
public class Like extends Common{
    @EmbeddedId
    private LikeId id;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private com.ssafy.backend.mysql.entity.Member member;

    @MapsId("trackId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private com.ssafy.backend.mysql.entity.Track track;


}