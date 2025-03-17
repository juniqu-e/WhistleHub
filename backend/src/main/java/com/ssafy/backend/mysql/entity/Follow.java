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
@Table(name = "follow")
public class Follow {
    @Id
    @Column(name = "follow_id", nullable = false)
    private Integer id;

    @Column(name = "from_member_id", nullable = false)
    private Integer fromMemberId;

    @Column(name = "to_member_id", nullable = false)
    private Integer toMemberId;

}