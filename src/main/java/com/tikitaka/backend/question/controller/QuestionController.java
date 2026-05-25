package com.tikitaka.backend.question.controller;

import com.tikitaka.backend.question.dto.*;
import com.tikitaka.backend.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class QuestionController {

    private final QuestionService questionService;

    // QA-001 질문 작성
    @PostMapping("/slides/{slideId}/questions")
    public QuestionCreateResponse createQuestion(
            @PathVariable UUID slideId,
            @RequestHeader("X-USER-ID") UUID studentId,
            @RequestBody QuestionCreateRequest request
    ) {
        return questionService.createQuestion(slideId, studentId, request);
    }

    // QA-002 특정 슬라이드 질문 목록 조회
    @GetMapping("/slides/{slideId}/questions")
    public List<QuestionListResponse> getQuestionsBySlide(
            @PathVariable UUID slideId
    ) {
        return questionService.getQuestionsBySlide(slideId);
    }

    // QA-003 슬라이드 오버레이용 질문 목록 조회
    @GetMapping("/slides/{slideId}/questions/overlay")
    public List<QuestionOverlayResponse> getQuestionOverlayBySlide(
            @PathVariable UUID slideId
    ) {
        return questionService.getQuestionOverlayBySlide(slideId);
    }

    // QA-004 특정 질문 및 답변 조회
    @GetMapping("/questions/{questionId}")
    public QuestionDetailResponse getQuestionDetail(
            @PathVariable UUID questionId
    ) {
        return questionService.getQuestionDetail(questionId);
    }

    // QA-008 교수 답변 작성
    @PostMapping("/questions/{questionId}/answer")
    public AnswerCreateResponse createAnswer(
            @PathVariable UUID questionId,
            @RequestHeader("X-USER-ID") UUID professorId,
            @RequestBody AnswerCreateRequest request
    ) {
        return questionService.createAnswer(questionId, professorId, request);
    }
}