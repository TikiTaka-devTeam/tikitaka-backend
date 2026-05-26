package com.tikitaka.backend.stroke.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class GetPrivateStrokesResponse {
    private UUID slideId;
    private String imageUrl;
    private List<StrokeResponse> strokes;
}
