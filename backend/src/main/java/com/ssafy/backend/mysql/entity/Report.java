package com.ssafy.backend.mysql.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "report")
public class Report extends Common{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private com.ssafy.backend.mysql.entity.Track track;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="member_id", nullable = false)
    private com.ssafy.backend.mysql.entity.Member member;

    @Column(name = "reason", nullable = false, length = 100)
    private int reportType;

    @Column(name = "detail", length = 300)
    private String detail;


}