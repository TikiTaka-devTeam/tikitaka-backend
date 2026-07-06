package com.tikitaka.backend.slide.service;

import com.tikitaka.backend.revision.entity.RevisionSlide;
import com.tikitaka.backend.revision.repository.RevisionSlideRepository;
import com.tikitaka.backend.slide.dto.InsertSlideResponse;
import com.tikitaka.backend.slide.dto.ReplaceSlideResponse;
import com.tikitaka.backend.slide.entity.Slide;
import com.tikitaka.backend.slide.repository.SlideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SlideRevisionService {

    private final SlideRepository slideRepository;
    private final RevisionSlideRepository revisionSlideRepository;

    public ReplaceSlideResponse replaceSlide(UUID professorId, UUID slideId, UUID revisionSlideId) {
        Slide slide = slideRepository.findById(slideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "교체할 기존 슬라이드를 찾을 수 없습니다."));

        if (Boolean.TRUE.equals(slide.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제된 슬라이드는 교체할 수 없습니다.");
        }

        if (Boolean.TRUE.equals(slide.getIsReplaced())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 교체 처리된 슬라이드입니다.");
        }

        UUID ownerProfessorId = slide.getDocument().getSpace().getProfessor().getId();
        if (!ownerProfessorId.equals(professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 강의자료의 슬라이드만 교체할 수 있습니다.");
        }

        RevisionSlide revisionSlide = revisionSlideRepository.findById(revisionSlideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "수정 슬라이드를 찾을 수 없습니다."));

        if (!revisionSlide.getDocumentRevision().getDocument().getId().equals(slide.getDocument().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "같은 강의자료에서 업로드한 수정 슬라이드만 교체에 사용할 수 있습니다.");
        }

        if (revisionSlideRepository.existsByTargetSlideIdAndIdNot(slideId, revisionSlideId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "해당 기존 슬라이드는 이미 다른 수정 슬라이드와 연결되어 있습니다.");
        }

        revisionSlide.assignTargetSlide(slide);

        return new ReplaceSlideResponse(
                slide.getId(),
                revisionSlide.getId(),
                slide.getDocument().getId(),
                slide.getPageNumber(),
                revisionSlide.getImageUrl()
        );
    }

    public InsertSlideResponse insertSlide(UUID professorId, UUID documentId, UUID revisionSlideId, UUID insertAfterSlideId) {
        Slide insertAfterSlide = slideRepository.findById(insertAfterSlideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "삽입 기준이 되는 기존 슬라이드를 찾을 수 없습니다."));

        if (!insertAfterSlide.getDocument().getId().equals(documentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삽입 기준 슬라이드가 해당 강의자료에 속하지 않습니다.");
        }

        if (Boolean.TRUE.equals(insertAfterSlide.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제된 슬라이드 뒤에는 삽입할 수 없습니다.");
        }

        UUID ownerProfessorId = insertAfterSlide.getDocument().getSpace().getProfessor().getId();
        if (!ownerProfessorId.equals(professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 강의자료의 슬라이드만 기준으로 삽입할 수 있습니다.");
        }

        RevisionSlide revisionSlide = revisionSlideRepository.findById(revisionSlideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "삽입할 수정 슬라이드를 찾을 수 없습니다."));

        if (!revisionSlide.getDocumentRevision().getDocument().getId().equals(documentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "같은 강의자료에서 업로드한 수정 슬라이드만 삽입에 사용할 수 있습니다.");
        }

        revisionSlide.assignInsertAfterSlide(insertAfterSlide);

        return new InsertSlideResponse(
                documentId,
                revisionSlide.getId(),
                insertAfterSlide.getId(),
                revisionSlide.getPageNumber(),
                revisionSlide.getImageUrl()
        );
    }
}
