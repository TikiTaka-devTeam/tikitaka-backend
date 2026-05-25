package com.tikitaka.backend.question.controller;

import com.tikitaka.backend.question.dto.*;
import com.tikitaka.backend.question.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Question API", description = "슬라이드 질문 및 답변 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class QuestionController {

    private final QuestionService questionService;

    @Operation(
            summary = "학생 질문 작성",
            description = "학생이 PDF 강의자료의 특정 슬라이드 위에 질문을 작성합니다. 질문은 private_stroke_id와 연결되며, x_ratio/y_ratio 위치값을 통해 슬라이드 위 질문 핀으로 표시됩니다."
    )
    @PostMapping("/slides/{slideId}/questions")
    public QuestionCreateResponse createQuestion(
            @PathVariable UUID slideId,
            @RequestBody QuestionCreateRequest request
    ) {
        return questionService.createQuestion(slideId, request);
    }

    @Operation(
            summary = "특정 슬라이드 질문 목록 조회",
            description = "PDF 강의자료 옆 질문 목록 패널에서 사용할 특정 슬라이드의 질문 목록을 조회합니다. 질문 내용, AI 정제 질문, 좋아요 수, 위치, 상태값을 반환합니다."
    )
    @GetMapping("/slides/{slideId}/questions")
    public List<QuestionListResponse> getQuestionsBySlide(
            @PathVariable UUID slideId
    ) {
        return questionService.getQuestionsBySlide(slideId);
    }

    @Operation(
            summary = "슬라이드 위 질문 마커 조회",
            description = "PDF 슬라이드 이미지 위에 표시할 질문 마커 목록을 조회합니다. 질문 위치인 x_ratio/y_ratio와 질문 내용, 좋아요 수를 반환합니다."
    )
    @GetMapping("/slides/{slideId}/questions/overlay")
    public List<QuestionOverlayResponse> getQuestionOverlayBySlide(
            @PathVariable UUID slideId
    ) {
        return questionService.getQuestionOverlayBySlide(slideId);
    }

    @Operation(
            summary = "특정 질문 및 답변 조회",
            description = "질문을 클릭했을 때 사용할 상세 조회 API입니다. 특정 질문의 내용, 정제된 질문, 좋아요 수, 상태, 답변 정보를 함께 조회합니다."
    )
    @GetMapping("/questions/{questionId}")
    public QuestionDetailResponse getQuestionDetail(
            @PathVariable UUID questionId
    ) {
        return questionService.getQuestionDetail(questionId);
    }

    @Operation(
            summary = "교수 답변 작성",
            description = "교수가 특정 질문에 답변을 작성합니다. 답변 작성 후 해당 질문의 상태는 ANSWERED로 변경됩니다."
    )
    @PostMapping("/questions/{questionId}/answer")
    public AnswerCreateResponse createAnswer(
            @PathVariable UUID questionId,
            @RequestBody AnswerCreateRequest request
    ) {
        return questionService.createAnswer(questionId, request);
    }
}