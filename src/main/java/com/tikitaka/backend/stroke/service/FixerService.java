package com.tikitaka.backend.stroke.service;

import com.tikitaka.backend.global.config.security.CurrentUserProvider;
import com.tikitaka.backend.layer.entity.PrivateLayer;
import com.tikitaka.backend.layer.repository.PrivateLayerRepository;
import com.tikitaka.backend.slide.entity.Slide;
import com.tikitaka.backend.slide.repository.SlideRepository;
import com.tikitaka.backend.stroke.dto.FixerCheckResponse;
import com.tikitaka.backend.stroke.dto.FixerCreateRequest;
import com.tikitaka.backend.stroke.dto.FixerCreateResponse;
import com.tikitaka.backend.stroke.dto.FixerListResponse;
import com.tikitaka.backend.stroke.entity.Fixer;
import com.tikitaka.backend.stroke.repository.FixerRepository;
import com.tikitaka.backend.user.entity.Role;
import com.tikitaka.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FixerService {

    private final FixerRepository fixerRepository;
    private final SlideRepository slideRepository;
    private final PrivateLayerRepository privateLayerRepository;
    private final CurrentUserProvider currentUserProvider;

    /**
     * FIXER-001
     * 교수 개인 레이어에 수정 메모 작성
     */
    @Transactional
    public FixerCreateResponse createFixer(
            UUID slideId,
            FixerCreateRequest request
    ) {
        User currentUser = currentUserProvider.getCurrentUser();

        validateProfessor(currentUser);
        validateCreateRequest(request);

        Slide slide = slideRepository.findById(slideId)
                .orElseThrow(() -> new IllegalArgumentException("슬라이드를 찾을 수 없습니다."));

        PrivateLayer layer = privateLayerRepository.findBySlideAndUser(slide, currentUser)
                .orElseGet(() -> privateLayerRepository.save(
                        PrivateLayer.builder()
                                .slide(slide)
                                .user(currentUser)
                                .build()
                ));

        Fixer fixer = Fixer.builder()
                .layer(layer)
                .professor(currentUser)
                .xRatio(request.getXRatio())
                .yRatio(request.getYRatio())
                .content(request.getContent())
                .isChecked(false)
                .build();

        Fixer savedFixer = fixerRepository.save(fixer);

        return FixerCreateResponse.from(savedFixer);
    }

    /**
     * FIXER-002
     * 교수 본인의 수정 메모 조회
     */
    public List<FixerListResponse> getFixersBySlide(UUID slideId) {
        User currentUser = currentUserProvider.getCurrentUser();

        validateProfessor(currentUser);

        return fixerRepository
                .findByLayerSlideIdAndProfessorIdOrderByCreatedAtDesc(
                        slideId,
                        currentUser.getId()
                )
                .stream()
                .map(FixerListResponse::from)
                .toList();
    }

    /**
     * FIXER-003
     * 수정 메모 완료 처리
     */
    @Transactional
    public FixerCheckResponse checkFixer(UUID fixerId) {
        User currentUser = currentUserProvider.getCurrentUser();

        validateProfessor(currentUser);

        Fixer fixer = fixerRepository.findByIdAndProfessorId(
                        fixerId,
                        currentUser.getId()
                )
                .orElseThrow(() -> new IllegalArgumentException("수정 메모를 찾을 수 없습니다."));

        fixer.check();

        return FixerCheckResponse.from(fixer);
    }

    private void validateProfessor(User user) {
        if (user.getRole() != Role.PROFESSOR) {
            throw new IllegalStateException("교수만 수정 메모를 사용할 수 있습니다.");
        }
    }

    private void validateCreateRequest(FixerCreateRequest request) {
        if (request.getXRatio() == null || request.getYRatio() == null) {
            throw new IllegalArgumentException("수정 메모 위치 정보가 필요합니다.");
        }

        if (request.getXRatio() < 0 || request.getXRatio() > 1) {
            throw new IllegalArgumentException("x_ratio는 0 이상 1 이하의 비율이어야 합니다.");
        }

        if (request.getYRatio() < 0 || request.getYRatio() > 1) {
            throw new IllegalArgumentException("y_ratio는 0 이상 1 이하의 비율이어야 합니다.");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("수정 메모 내용이 필요합니다.");
        }
    }
}