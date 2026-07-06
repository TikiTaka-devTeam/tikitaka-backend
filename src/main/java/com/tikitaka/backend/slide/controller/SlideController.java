package com.tikitaka.backend.slide.controller;

import com.tikitaka.backend.slide.dto.SlideListResponse;
import com.tikitaka.backend.slide.service.SlideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Slide API", description = "강의자료 슬라이드 조회 및 수정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SlideController {

    private final SlideService slideService;

    @Operation(
            summary = "강의자료 슬라이드 목록 조회",
            description = "특정 강의자료(document)의 슬라이드 목록을 페이지 번호 순서대로 조회합니다. 질문 API 테스트에 필요한 slide_id를 이 API에서 확인할 수 있습니다."
    )
    @GetMapping("/documents/{documentId}/slides")
    public List<SlideListResponse> getSlidesByDocument(
            @Parameter(description = "강의자료 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("documentId") UUID documentId
    ) {
        return slideService.getSlidesByDocument(documentId);
    }
}
