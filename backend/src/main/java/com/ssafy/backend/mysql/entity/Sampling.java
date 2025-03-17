package com.ssafy.backend.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sampling")
public class Sampling {
    @Id
    @Column(name = "sampling_id", nullable = false)
    private Integer id;

    @Column(name = "origin_track_id", nullable = false)
    private Integer originTrackId;

    @Column(name = "track_id", nullable = false)
    private Integer trackId;

}