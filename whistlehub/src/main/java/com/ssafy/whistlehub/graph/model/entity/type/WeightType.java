package com.ssafy.whistlehub.graph.model.entity.type;

import lombok.Getter;

@Getter
public enum WeightType {
    VIEW(1),
    LIKE(10),
    DISLIKE(-10);

    private final int value;

    WeightType(int value) {
        this.value = value;
    }

}
