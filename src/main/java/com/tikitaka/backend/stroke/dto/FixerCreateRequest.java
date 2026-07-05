package com.tikitaka.backend.stroke.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "수정 메모 작성 요청")
public class FixerCreateRequest {

    @Schema(
            description = "슬라이드 내 X 좌표 비율. 0 이상 1 이하 값",
            example = "0.35"
    )
    @JsonProperty("x_ratio")
    private Double xRatio;

    @Schema(
            description = "슬라이드 내 Y 좌표 비율. 0 이상 1 이하 값",
            example = "0.62"
    )
    @JsonProperty("y_ratio")
    private Double yRatio;

    @Schema(
            description = "수정 메모 내용",
            example = "이 부분 수식 수정 필요"
    )
    private String content;
}