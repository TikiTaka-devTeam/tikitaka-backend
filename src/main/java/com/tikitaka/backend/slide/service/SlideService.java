package com.tikitaka.backend.slide.service;

import com.tikitaka.backend.document.entity.Document;
import com.tikitaka.backend.document.repository.DocumentRepository;
import com.tikitaka.backend.slide.dto.SlideListResponse;
import com.tikitaka.backend.slide.repository.SlideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlideService {

    private final SlideRepository slideRepository;
    private final DocumentRepository documentRepository;

    public List<SlideListResponse> getSlidesByDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("강의자료를 찾을 수 없습니다."));

        return slideRepository.findByDocumentIdOrderByPageNumberAsc(document.getId())
                .stream()
                .map(SlideListResponse::from)
                .toList();
    }
}