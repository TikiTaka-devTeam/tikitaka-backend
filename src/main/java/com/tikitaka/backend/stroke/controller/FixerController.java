package com.tikitaka.backend.stroke.controller;

import com.tikitaka.backend.stroke.dto.FixerCheckResponse;
import com.tikitaka.backend.stroke.dto.FixerCreateRequest;
import com.tikitaka.backend.stroke.dto.FixerCreateResponse;
import com.tikitaka.backend.stroke.dto.FixerListResponse;
import com.tikitaka.backend.stroke.service.FixerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FixerController {

    private final FixerService fixerService;

    /**
     * FIXER-001
     * 교수 개인 레이어에 수정 메모 작성
     *
     * POST /api/v1/slides/{slide_id}/fixers
     */
    @PostMapping("/slides/{slide_id}/fixers")
    public FixerCreateResponse createFixer(
            @PathVariable("slide_id") UUID slideId,
            @RequestBody FixerCreateRequest request
    ) {
        return fixerService.createFixer(slideId, request);
    }

    /**
     * FIXER-002
     * 교수 본인의 수정 메모 조회
     *
     * GET /api/v1/slides/{slide_id}/fixers
     */
    @GetMapping("/slides/{slide_id}/fixers")
    public List<FixerListResponse> getFixersBySlide(
            @PathVariable("slide_id") UUID slideId
    ) {
        return fixerService.getFixersBySlide(slideId);
    }

    /**
     * FIXER-003
     * 수정 메모 완료 처리
     *
     * PATCH /api/v1/fixers/{fixer_id}/check
     */
    @PatchMapping("/fixers/{fixer_id}/check")
    public FixerCheckResponse checkFixer(
            @PathVariable("fixer_id") UUID fixerId
    ) {
        return fixerService.checkFixer(fixerId);
    }
}