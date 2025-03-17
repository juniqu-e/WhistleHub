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
public class ReportId implements java.io.Serializable {
    private static final long serialVersionUID = 5543536265980076222L;
    @Column(name = "report_id", nullable = false)
    private Integer reportId;

    @Column(name = "track_id", nullable = false)
    private Integer trackId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ReportId entity = (ReportId) o;
        return Objects.equals(this.reportId, entity.reportId) &&
                Objects.equals(this.trackId, entity.trackId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId, trackId);
    }

}