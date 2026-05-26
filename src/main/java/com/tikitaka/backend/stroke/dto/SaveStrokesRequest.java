package com.tikitaka.backend.stroke.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class SaveStrokesRequest {

    @NotNull(message = "strokes는 필수입니다")
    @Valid
    private List<StrokeRequest> strokes;
}
