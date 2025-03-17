package com.ssafy.backend.mysql.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "layer_file")
public class LayerFile extends Common{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "layer_file_id", nullable = false)
    private Integer id;

    @Column(name = "sound_url", nullable = false)
    private String soundUrl;


}