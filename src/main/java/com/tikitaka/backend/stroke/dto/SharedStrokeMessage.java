package com.tikitaka.backend.stroke.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedStrokeMessage {

    private String type;
    private UUID strokeId;
    private UUID slideId;
    private StrokeData stroke;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StrokeData {
        private String tool;
        private List<StrokePoint> points;
        private String color;
        private Float thickness;
        private String content;
        private Integer strokeOrder;
    }
}
