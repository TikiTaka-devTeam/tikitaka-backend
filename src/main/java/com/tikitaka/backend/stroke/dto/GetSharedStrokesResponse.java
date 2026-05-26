package com.tikitaka.backend.stroke.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class GetSharedStrokesResponse {
    private UUID slideId;
    private Boolean isVisible;
    private List<StrokeResponse> strokes;
}
