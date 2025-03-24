package com.ssafy.backend.common.prop;

import lombok.Getter;

@Getter
public enum InstrumentType {
    GUITAR(1),
    DRUM(2),
    BASE(3),
    PIANO(4);

    private final int type;

    InstrumentType(int value) {
        this.type = value;
    }
}
