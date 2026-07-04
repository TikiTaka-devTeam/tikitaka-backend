package com.tikitaka.backend.space.dto;

import jakarta.validation.constraints.Size;

public record UpdateSpaceNoticeRequest(

        @Size(max = 255, message = "공지 제목은 255자를 넘을 수 없습니다.")
        String title,

        String content,

        Boolean isPinned
) {
}