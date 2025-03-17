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
@Table(name = "tag")
public class Tag extends Common{
    @Id
    @Column(name = "tag_id", nullable = false)
    private Integer id;

    @Column(name = "name")
    private String name;


}