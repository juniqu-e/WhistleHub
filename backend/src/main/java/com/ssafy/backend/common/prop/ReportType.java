package com.ssafy.backend.common.prop;

import lombok.Getter;

@Getter
public enum ReportType {
    COPYRIGHT(1),
    BAD_TRACK(2);

    private final int type;

    ReportType(int value) {
        this.type = value;
    }
}
