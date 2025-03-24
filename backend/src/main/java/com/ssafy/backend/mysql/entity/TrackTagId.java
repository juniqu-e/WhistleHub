package com.ssafy.backend.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class TrackTagId implements java.io.Serializable {
    private static final long serialVersionUID = 3005475495967430234L;
    @Column(name = "tag_id", nullable = false)
    private Integer tagId;

    @Column(name = "track_id", nullable = false)
    private Integer trackId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TrackTagId entity = (TrackTagId) o;
        return Objects.equals(this.tagId, entity.tagId) &&
                Objects.equals(this.trackId, entity.trackId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId, trackId);
    }

}