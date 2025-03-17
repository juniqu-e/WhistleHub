package com.ssafy.backend.mysql.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reported_layer")
public class ReportedLayer {
    @EmbeddedId
    private ReportedLayerId id;

    @MapsId("reportId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false, referencedColumnName = "report_id")
    private Report report;

    @MapsId("layerId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "layer_id", nullable = false)
    private Layer layer;

}