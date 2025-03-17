package com.ssafy.backend.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class CommentId implements java.io.Serializable {
    private static final long serialVersionUID = -218119149161228547L;
    @Column(name = "comment_id", nullable = false)
    private Integer commentId;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(name = "track_id", nullable = false)
    private Integer trackId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CommentId entity = (CommentId) o;
        return Objects.equals(this.trackId, entity.trackId) &&
                Objects.equals(this.commentId, entity.commentId) &&
                Objects.equals(this.memberId, entity.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackId, commentId, memberId);
    }

}