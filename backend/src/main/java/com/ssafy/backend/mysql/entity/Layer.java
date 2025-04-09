package com.ssafy.backend.mysql.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "layer")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Layer extends Common{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "layer_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private com.ssafy.backend.mysql.entity.Track track;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "instrument_type", length = 100)
    private int instrumentType;

    @Column(name = "modification")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> modification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layer_file_id")
    private com.ssafy.backend.mysql.entity.LayerFile layerFile;

    @Column(name = "blocked", nullable = false)
    private Boolean blocked = false;

    @Column(name = "bars")
    private String bars;

}