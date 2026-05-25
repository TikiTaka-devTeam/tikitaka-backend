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

    // QA-001 학생이 PDF 강의자료 특정 슬라이드 위에 질문 작성
    @PostMapping("/slides/{slideId}/questions")
    public QuestionCreateResponse createQuestion(
            @PathVariable UUID slideId,
            @RequestBody QuestionCreateRequest request
    ) {
        return questionService.createQuestion(slideId, request);
    }

    // QA-002 강의자료 옆 질문 목록 패널 조회
    @GetMapping("/slides/{slideId}/questions")
    public List<QuestionListResponse> getQuestionsBySlide(
            @PathVariable UUID slideId
    ) {
        return questionService.getQuestionsBySlide(slideId);
    }

    // QA-003 PDF 슬라이드 위 질문 마커 조회
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
            @RequestBody AnswerCreateRequest request
    ) {
        return questionService.createAnswer(questionId, request);
    }
}