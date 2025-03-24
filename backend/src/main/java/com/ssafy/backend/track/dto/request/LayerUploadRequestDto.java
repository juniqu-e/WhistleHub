package com.ssafy.backend.track.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LayerUploadRequestDto {
    String name;
    @NotBlank
    int instrumentType;
}
