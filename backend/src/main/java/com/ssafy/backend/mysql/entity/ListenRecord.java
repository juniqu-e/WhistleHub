package com.ssafy.backend.mysql.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "listen_record")
public class ListenRecord extends Common{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listen_record_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private com.ssafy.backend.mysql.entity.Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private com.ssafy.backend.mysql.entity.Track track;


}