package com.tikitaka.backend.space.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSpaceNoticeRequest(

        @NotBlank(message = "공지 제목은 필수입니다.")
        @Size(max = 255, message = "공지 제목은 255자를 넘을 수 없습니다.")
        String title,

        @NotBlank(message = "공지 내용은 필수입니다.")
        String content,

        Boolean isPinned
) {
}