package com.tikitaka.backend.revision.service;

import com.tikitaka.backend.document.entity.Document;
import com.tikitaka.backend.document.repository.DocumentRepository;
import com.tikitaka.backend.global.storage.S3StorageService;
import com.tikitaka.backend.revision.dto.CreateDocumentReplacementResponse;
import com.tikitaka.backend.revision.dto.ReplacementSlideResponse;
import com.tikitaka.backend.revision.entity.DocumentRevision;
import com.tikitaka.backend.revision.entity.RevisionSlide;
import com.tikitaka.backend.revision.repository.DocumentRevisionRepository;
import com.tikitaka.backend.revision.repository.RevisionSlideRepository;
import com.tikitaka.backend.slide.dto.PdfSlideConvertResult;
import com.tikitaka.backend.slide.service.PdfSlideConvertService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class DocumentRevisionService {

    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository documentRevisionRepository;
    private final RevisionSlideRepository revisionSlideRepository;
    private final S3StorageService s3StorageService;
    private final PdfSlideConvertService pdfSlideConvertService;

    public DocumentRevisionService(
            DocumentRepository documentRepository,
            DocumentRevisionRepository documentRevisionRepository,
            RevisionSlideRepository revisionSlideRepository,
            S3StorageService s3StorageService,
            PdfSlideConvertService pdfSlideConvertService
    ) {
        this.documentRepository = documentRepository;
        this.documentRevisionRepository = documentRevisionRepository;
        this.revisionSlideRepository = revisionSlideRepository;
        this.s3StorageService = s3StorageService;
        this.pdfSlideConvertService = pdfSlideConvertService;
    }

    public CreateDocumentReplacementResponse createReplacement(
            UUID professorId,
            UUID documentId,
            MultipartFile file
    ) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "강의자료를 찾을 수 없습니다."));

        UUID ownerProfessorId = document.getSpace().getProfessor().getId();
        if (!ownerProfessorId.equals(professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 강의의 강의자료만 수정할 수 있습니다.");
        }

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF 파일은 필수입니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF 파일만 업로드할 수 있습니다.");
        }

        String documentFileKey = UUID.randomUUID().toString();
        Path tempPdfPath = null;

        try {
            tempPdfPath = Files.createTempFile("lecture-revision-" + documentFileKey + "-", ".pdf");
            Files.copy(file.getInputStream(), tempPdfPath, StandardCopyOption.REPLACE_EXISTING);

            String replacementPdfUrl = s3StorageService.uploadLecturePdf(
                    documentFileKey,
                    originalFilename,
                    file
            );

            PdfSlideConvertResult convertResult = pdfSlideConvertService.convertPdfToSlideImages(
                    tempPdfPath,
                    documentFileKey
            );

            DocumentRevision documentRevision = documentRevisionRepository.save(
                    DocumentRevision.builder()
                            .document(document)
                            .replacementPdfUrl(replacementPdfUrl)
                            .replacementPdfPageCount(convertResult.pageCount())
                            .build()
            );

            List<ReplacementSlideResponse> replacementSlides = new ArrayList<>();

            for (int i = 0; i < convertResult.slideImageUrls().size(); i++) {
                RevisionSlide revisionSlide = revisionSlideRepository.save(
                        RevisionSlide.builder()
                                .documentRevision(documentRevision)
                                .pageNumber(i + 1)
                                .imageUrl(convertResult.slideImageUrls().get(i))
                                .build()
                );

                replacementSlides.add(new ReplacementSlideResponse(
                        revisionSlide.getId(),
                        revisionSlide.getPageNumber(),
                        revisionSlide.getImageUrl()
                ));
            }

            return new CreateDocumentReplacementResponse(
                    documentRevision.getId(),
                    document.getId(),
                    documentRevision.getReplacementPdfUrl(),
                    replacementSlides
            );
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "수정용 강의자료 업로드 중 오류가 발생했습니다."
            );
        } finally {
            if (tempPdfPath != null) {
                try {
                    Files.deleteIfExists(tempPdfPath);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
