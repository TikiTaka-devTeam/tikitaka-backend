package com.tikitaka.backend.question.service;

import com.tikitaka.backend.global.config.security.CurrentUserProvider;
import com.tikitaka.backend.question.ai.OpenAiClient;
import com.tikitaka.backend.question.answer.Answer;
import com.tikitaka.backend.question.audio.AnswerAudioFile;
import com.tikitaka.backend.question.dto.*;
import com.tikitaka.backend.question.entity.Question;
import com.tikitaka.backend.question.entity.QuestionLike;
import com.tikitaka.backend.question.enums.QuestionStatus;
import com.tikitaka.backend.question.repository.AnswerAudioFileRepository;
import com.tikitaka.backend.question.repository.AnswerRepository;
import com.tikitaka.backend.question.repository.QuestionLikeRepository;
import com.tikitaka.backend.question.repository.QuestionRepository;
import com.tikitaka.backend.slide.entity.Slide;
import com.tikitaka.backend.slide.repository.SlideRepository;
import com.tikitaka.backend.stroke.entity.PrivateStroke;
import com.tikitaka.backend.stroke.repository.PrivateStrokeRepository;
import com.tikitaka.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final AnswerAudioFileRepository answerAudioFileRepository;
    private final SlideRepository slideRepository;
    private final PrivateStrokeRepository privateStrokeRepository;
    private final QuestionLikeRepository questionLikeRepository;
    private final OpenAiClient openAiClient;
    private final CurrentUserProvider currentUserProvider;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public QuestionCreateResponse createQuestion(
            UUID slideId,
            QuestionCreateRequest request
    ) {
        User currentUser = currentUserProvider.getCurrentUser();

        if (!isStudent(currentUser)) {
            throw new IllegalStateException("학생만 질문을 작성할 수 있습니다.");
        }

        Slide slide = slideRepository.findById(slideId)
                .orElseThrow(() -> new IllegalArgumentException("슬라이드를 찾을 수 없습니다."));

        PrivateStroke privateStroke = privateStrokeRepository.findById(request.getPrivateStrokeId())
                .orElseThrow(() -> new IllegalArgumentException("질문 포인트 필기를 찾을 수 없습니다."));

        Question question = Question.builder()
                .slide(slide)
                .student(currentUser)
                .privateStroke(privateStroke)
                .content(request.getContent())
                .isAnonymous(request.getIsAnonymous())
                .xRatio(request.getXRatio())
                .yRatio(request.getYRatio())
                .likeCount(0)
                .status(QuestionStatus.PENDING)
                .isRefined(false)
                .build();

        Question savedQuestion = questionRepository.save(question);

        publishQuestionCreated(savedQuestion);

        return QuestionCreateResponse.from(savedQuestion);
    }

    public List<QuestionListResponse> getQuestionsBySlide(UUID slideId) {
        User currentUser = currentUserProvider.getCurrentUser();

        return questionRepository.findBySlideIdOrderByCreatedAtDesc(slideId)
                .stream()
                .map(question -> QuestionListResponse.from(
                        question,
                        questionLikeRepository.existsByQuestionAndUser(question, currentUser)
                ))
                .toList();
    }

    public List<QuestionListResponse> getQuestionsByDocument(UUID documentId) {
        User currentUser = currentUserProvider.getCurrentUser();

        return questionRepository.findBySlideDocumentIdOrderByCreatedAtDesc(documentId)
                .stream()
                .map(question -> QuestionListResponse.from(
                        question,
                        questionLikeRepository.existsByQuestionAndUser(question, currentUser)
                ))
                .toList();
    }

    public List<QuestionOverlayResponse> getQuestionOverlayBySlide(UUID slideId) {
        return questionRepository.findBySlideIdOrderByCreatedAtDesc(slideId)
                .stream()
                .map(QuestionOverlayResponse::from)
                .toList();
    }

    public QuestionDetailResponse getQuestionDetail(UUID questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));

        Answer answer = answerRepository.findByQuestionId(questionId)
                .orElse(null);

        return QuestionDetailResponse.from(question, answer);
    }

    @Transactional
    public QuestionLikeResponse likeQuestion(UUID questionId) {
        User currentUser = currentUserProvider.getCurrentUser();

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));

        if (questionLikeRepository.existsByQuestionAndUser(question, currentUser)) {
            return QuestionLikeResponse.of(question.getId(), true, question.getLikeCount());
        }

        QuestionLike questionLike = QuestionLike.builder()
                .question(question)
                .user(currentUser)
                .build();

        questionLikeRepository.save(questionLike);
        question.increaseLikeCount();

        return QuestionLikeResponse.of(question.getId(), true, question.getLikeCount());
    }

    @Transactional
    public QuestionLikeResponse unlikeQuestion(UUID questionId) {
        User currentUser = currentUserProvider.getCurrentUser();

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));

        questionLikeRepository.findByQuestionAndUser(question, currentUser)
                .ifPresent(questionLike -> {
                    questionLikeRepository.delete(questionLike);
                    question.decreaseLikeCount();
                });

        return QuestionLikeResponse.of(question.getId(), false, question.getLikeCount());
    }

    @Transactional
    public AnswerCreateResponse createAnswer(
            UUID questionId,
            AnswerCreateRequest request
    ) {
        User currentUser = currentUserProvider.getCurrentUser();

        if (!isProfessor(currentUser)) {
            throw new IllegalStateException("교수만 답변을 작성할 수 있습니다.");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));

        if (answerRepository.existsByQuestionId(questionId)) {
            throw new IllegalStateException("이미 답변이 작성된 질문입니다.");
        }

        Answer answer = Answer.builder()
                .question(question)
                .professor(currentUser)
                .answererType("PROFESSOR")
                .content(request.getContent())
                .aiModel(null)
                .build();

        Answer savedAnswer = answerRepository.save(answer);

        question.markAsAnswered();

        return AnswerCreateResponse.from(savedAnswer);
    }

    @Transactional
    public AnswerUpdateResponse updateAnswer(
            UUID answerId,
            AnswerUpdateRequest request
    ) {
        User currentUser = currentUserProvider.getCurrentUser();

        if (!isProfessor(currentUser)) {
            throw new IllegalStateException("교수만 답변을 수정할 수 있습니다.");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("답변을 찾을 수 없습니다."));

        if (!"PROFESSOR".equals(answer.getAnswererType())) {
            throw new IllegalStateException("교수 답변만 수정할 수 있습니다.");
        }

        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("답변 내용은 비어 있을 수 없습니다.");
        }

        answer.updateContent(request.content());

        return AnswerUpdateResponse.from(answer);
    }

    @Transactional
    public AnswerAudioCreateResponse createAnswerAudio(
            UUID answerId,
            AnswerAudioCreateRequest request
    ) {
        User currentUser = currentUserProvider.getCurrentUser();

        if (!isProfessor(currentUser)) {
            throw new IllegalStateException("교수만 음성 답변을 등록할 수 있습니다.");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("답변을 찾을 수 없습니다."));

        if (!"PROFESSOR".equals(answer.getAnswererType())) {
            throw new IllegalStateException("교수 답변에만 음성 답변을 등록할 수 있습니다.");
        }

        if (request.audioUrl() == null || request.audioUrl().isBlank()) {
            throw new IllegalArgumentException("음성 파일 URL은 비어 있을 수 없습니다.");
        }

        AnswerAudioFile audioFile = AnswerAudioFile.builder()
                .answer(answer)
                .audioUrl(request.audioUrl())
                .build();

        AnswerAudioFile savedAudioFile = answerAudioFileRepository.save(audioFile);

        return AnswerAudioCreateResponse.from(savedAudioFile);
    }

    @Transactional
    public AiAnswerCreateResponse createAiAnswer(UUID questionId) {
        User currentUser = currentUserProvider.getCurrentUser();

        if (!isProfessor(currentUser)) {
            throw new IllegalStateException("교수만 AI 자동 답변을 생성할 수 있습니다.");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));

        if (answerRepository.existsByQuestionId(questionId)) {
            throw new IllegalStateException("이미 답변이 작성된 질문입니다.");
        }

        String lectureContext = "";
        String aiAnswerContent = openAiClient.generateAnswer(question.getContent(), lectureContext);

        Answer answer = Answer.builder()
                .question(question)
                .professor(null)
                .answererType("AI")
                .content(aiAnswerContent)
                .aiModel(openAiClient.getModel())
                .build();

        Answer savedAnswer = answerRepository.save(answer);

        question.markAsAnswered();

        return AiAnswerCreateResponse.from(savedAnswer);
    }

    public AnswerAudioSttStatusResponse getAnswerAudioSttStatus(UUID audioFileId) {
        AnswerAudioFile audioFile = answerAudioFileRepository.findById(audioFileId)
                .orElseThrow(() -> new IllegalArgumentException("음성 답변 파일을 찾을 수 없습니다."));

        return AnswerAudioSttStatusResponse.from(audioFile);
    }

    private void publishQuestionCreated(Question question) {
        UUID slideId = question.getSlide().getId();
        UUID spaceId = question.getSlide().getDocument().getSpace().getId();

        QuestionCreatedMessage broadcast = QuestionCreatedMessage.builder()
                .type("QUESTION_CREATED")
                .slideId(slideId)
                .questionId(question.getId())
                .question(QuestionListResponse.from(question))
                .build();

        messagingTemplate.convertAndSend(
                "/topic/spaces/" + spaceId + "/slides/" + slideId + "/questions",
                broadcast
        );
    }

    private boolean isStudent(User user) {
        return "STUDENT".equals(String.valueOf(user.getRole()));
    }

    private boolean isProfessor(User user) {
        return "PROFESSOR".equals(String.valueOf(user.getRole()));
    }
}