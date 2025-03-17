package com.ssafy.backend.mysql.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "layer")
public class Layer extends Common{
    @Id
    @Column(name = "layer_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private com.ssafy.backend.mysql.entity.Track track;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "instrument_type", length = 100)
    private String instrumentType;

    @Column(name = "modification")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> modification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layer_file_id")
    private com.ssafy.backend.mysql.entity.LayerFile layerFile;

    @Column(name = "blocked", nullable = false)
    private Boolean blocked = false;


}