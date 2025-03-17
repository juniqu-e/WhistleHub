package com.ssafy.backend.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "layer")
public class Layer {
    @Id
    @Column(name = "layer_id", nullable = false)
    private Integer id;

    @Column(name = "track_id", nullable = false)
    private Integer trackId;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "instrument_type", length = 100)
    private String instrumentType;

    @Column(name = "modification")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> modification;

    @Column(name = "layer_file_id")
    private Integer layerFileId;

    @Column(name = "blocked", nullable = false)
    private Boolean blocked = false;

}