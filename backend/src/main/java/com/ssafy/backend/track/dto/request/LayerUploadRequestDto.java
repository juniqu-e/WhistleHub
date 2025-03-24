package com.ssafy.backend.track.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LayerUploadRequestDto {
    @NotBlank
    String name;
    @NotBlank
    int instrumentType;
}
