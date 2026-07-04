package com.tikitaka.backend.space.dto;

import com.tikitaka.backend.space.entity.SpaceNotice;

import java.time.LocalDateTime;
import java.util.UUID;

public record SpaceNoticeResponse(
        UUID noticeId,
        UUID spaceId,
        UUID professorId,
        String professorName,
        String title,
        String content,
        Boolean isPinned,
        LocalDateTime createdAt,
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