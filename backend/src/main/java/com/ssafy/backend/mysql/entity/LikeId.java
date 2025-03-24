package com.ssafy.backend.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class LikeId implements java.io.Serializable {
    private static final long serialVersionUID = -7989052852313785267L;
    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(name = "track_id", nullable = false)
    private Integer trackId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        LikeId entity = (LikeId) o;
        return Objects.equals(this.trackId, entity.trackId) &&
                Objects.equals(this.memberId, entity.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackId, memberId);
    }

}