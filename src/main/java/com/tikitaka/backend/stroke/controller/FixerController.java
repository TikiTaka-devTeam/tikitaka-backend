package com.tikitaka.backend.stroke.controller;

import com.tikitaka.backend.stroke.dto.FixerCheckResponse;
import com.tikitaka.backend.stroke.dto.FixerCreateRequest;
import com.tikitaka.backend.stroke.dto.FixerCreateResponse;
import com.tikitaka.backend.stroke.dto.FixerListResponse;
import com.tikitaka.backend.stroke.service.FixerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Fixer", description = "교수 개인 수정 메모 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FixerController {

    private final FixerService fixerService;

    @Operation(
            summary = "수정 메모 작성",
            description = """
                    교수 개인 레이어에 수정 메모를 작성합니다.
                    
                    - 교수만 사용 가능합니다.
                    - 학생에게는 노출되지 않습니다.
                    - x_ratio, y_ratio는 슬라이드 내 좌표 비율이며 0 이상 1 이하 값입니다.
                    - 수정 아이콘 선택 후 클릭한 위치를 기준으로 메모를 저장합니다.
                    """
    )
    @PostMapping("/slides/{slide_id}/fixers")
    public FixerCreateResponse createFixer(
            @PathVariable("slide_id") UUID slideId,
            @RequestBody FixerCreateRequest request
    ) {
        return fixerService.createFixer(slideId, request);
    }

    @Operation(
            summary = "수정 메모 조회",
            description = """
                    특정 슬라이드에 작성된 교수 본인의 수정 메모 목록을 조회합니다.
                    
                    - 교수만 조회 가능합니다.
                    - 현재 로그인한 교수 본인이 작성한 수정 메모만 조회됩니다.
                    - 학생은 조회할 수 없습니다.
                    """
    )
    @GetMapping("/slides/{slide_id}/fixers")
    public List<FixerListResponse> getFixersBySlide(
            @PathVariable("slide_id") UUID slideId
    ) {
        return fixerService.getFixersBySlide(slideId);
    }

    @Operation(
            summary = "수정 메모 완료 처리",
            description = """
                    수정 메모를 완료 처리합니다.
                    
                    - 교수만 사용할 수 있습니다.
                    - 현재 로그인한 교수 본인의 수정 메모만 완료 처리할 수 있습니다.
                    - is_checked 값이 true로 변경됩니다.
                    """
    )
    @PatchMapping("/fixers/{fixer_id}/check")
    public FixerCheckResponse checkFixer(
            @PathVariable("fixer_id") UUID fixerId
    ) {
        return fixerService.checkFixer(fixerId);
    }
}