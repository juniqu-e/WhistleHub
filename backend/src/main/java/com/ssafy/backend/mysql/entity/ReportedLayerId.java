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
public class ReportedLayerId implements java.io.Serializable {
    private static final long serialVersionUID = 4719106299439663394L;
    @Column(name = "reported_layer_id", nullable = false)
    private Integer reportedLayerId;

    @Column(name = "report_id", nullable = false)
    private Integer reportId;

    @Column(name = "layer_id", nullable = false)
    private Integer layerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ReportedLayerId entity = (ReportedLayerId) o;
        return Objects.equals(this.reportedLayerId, entity.reportedLayerId) &&
                Objects.equals(this.layerId, entity.layerId) &&
                Objects.equals(this.reportId, entity.reportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportedLayerId, layerId, reportId);
    }

}