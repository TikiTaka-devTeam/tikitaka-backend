package com.tikitaka.backend.stroke.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class StrokeRequest {

    @NotBlank(message = "tool은 필수입니다")
    private String tool;

    @NotNull(message = "points는 필수입니다")
    private List<StrokePoint> points;

    private String content;
    private String color = "#000000";
    private Float thickness = 2.0f;
    private Integer strokeOrder = 0;
}
