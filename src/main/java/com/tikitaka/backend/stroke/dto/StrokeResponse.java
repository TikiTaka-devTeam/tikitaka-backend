package com.tikitaka.backend.stroke.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikitaka.backend.stroke.entity.PrivateStroke;
import com.tikitaka.backend.stroke.entity.SharedStroke;
import lombok.Builder;
import lombok.Getter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class StrokeResponse {

    private UUID strokeId;
    private String tool;
    private List<StrokePoint> points;
    private String content;
    private String color;
    private Float thickness;
    private Integer strokeOrder;
    private Integer strokeSeq;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static StrokeResponse from(PrivateStroke stroke) {
        return StrokeResponse.builder()
                .strokeId(stroke.getId())
                .tool(stroke.getTool())
                .points(parsePoints(stroke.getPoints()))
                .content(stroke.getContent())
                .color(stroke.getColor())
                .thickness(stroke.getThickness())
                .strokeOrder(stroke.getStrokeOrder())
                .build();
    }

    public static StrokeResponse from(SharedStroke stroke) {
        return StrokeResponse.builder()
                .strokeId(stroke.getId())
                .tool(stroke.getTool())
                .points(parsePoints(stroke.getPoints()))
                .content(stroke.getContent())
                .color(stroke.getColor())
                .thickness(stroke.getThickness())
                .strokeOrder(stroke.getStrokeOrder())
                .strokeSeq(stroke.getStrokeSeq())
                .build();
    }

    private static List<StrokePoint> parsePoints(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<StrokePoint>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
