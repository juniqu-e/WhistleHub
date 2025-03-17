package com.ssafy.backend.mysql.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "report")
public class Report {
    @EmbeddedId
    private ReportId id;

    @MapsId("trackId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private com.ssafy.backend.mysql.entity.Track track;

    @Column(name = "reason", nullable = false, length = 100)
    private String reason;

    @Column(name = "detail", length = 300)
    private String detail;

}