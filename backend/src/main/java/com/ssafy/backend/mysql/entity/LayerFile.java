package com.ssafy.backend.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "layer_file")
public class LayerFile extends Common{
    @Id
    @Column(name = "layer_file_id", nullable = false)
    private Integer id;

    @Column(name = "sound_url", nullable = false)
    private String soundUrl;


}