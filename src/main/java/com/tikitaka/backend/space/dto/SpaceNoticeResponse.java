package com.tikitaka.backend.space.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.space.entity.SpaceNotice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "스페이스 공지 응답")
public record SpaceNoticeResponse(

        @Schema(description = "공지 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @JsonProperty("notice_id")
        UUID noticeId,

        @Schema(description = "강의 ID", example = "123e4567-e89b-12d3-a456-426614174111")
        @JsonProperty("space_id")
        UUID spaceId,

        @Schema(description = "교수 ID", example = "123e4567-e89b-12d3-a456-426614174222")
        @JsonProperty("professor_id")
        UUID professorId,

        @Schema(description = "교수 이름", example = "김승훈")
        @JsonProperty("professor_name")
        String professorName,

        @Schema(description = "공지 제목", example = "중간고사 안내")
        String title,

        @Schema(description = "공지 내용", example = "다음 주 중간고사를 진행합니다.")
        String content,

        @Schema(description = "공지 고정 여부", example = "false")
        @JsonProperty("is_pinned")
        Boolean isPinned,

        @Schema(description = "공지 생성 시간", example = "2026-05-04T10:00:00")
        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @Schema(description = "공지 수정 시간", example = "2026-05-04T10:00:00")
        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {

    public static SpaceNoticeResponse from(SpaceNotice notice) {
        return new SpaceNoticeResponse(
                notice.getId(),
                notice.getSpace().getId(),
                notice.getProfessor().getId(),
                notice.getProfessor().getName(),
                notice.getTitle(),
                notice.getContent(),
                notice.getIsPinned(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}