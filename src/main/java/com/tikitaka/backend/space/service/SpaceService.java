package com.tikitaka.backend.space.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.tikitaka.backend.document.entity.Document;
import com.tikitaka.backend.document.repository.DocumentRepository;
import com.tikitaka.backend.global.storage.S3StorageService;
import com.tikitaka.backend.space.dto.DocumentSummaryResponse;

import com.tikitaka.backend.slide.dto.PdfSlideConvertResult;
import com.tikitaka.backend.slide.entity.Slide;
import com.tikitaka.backend.slide.repository.SlideRepository;
import com.tikitaka.backend.slide.service.PdfSlideConvertService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tikitaka.backend.space.dto.CreateScheduleRequest;
import com.tikitaka.backend.space.dto.CreateSpaceRequest;
import com.tikitaka.backend.space.dto.CreateSpaceResponse;
import com.tikitaka.backend.space.dto.JoinSpaceRequest;
import com.tikitaka.backend.space.dto.JoinSpaceResponse;
import com.tikitaka.backend.space.dto.SpaceCodeResponse;
import com.tikitaka.backend.space.dto.SpaceLookupResponse;
import com.tikitaka.backend.space.dto.SpaceLookupScheduleResponse;
import com.tikitaka.backend.space.dto.SpaceMemberStatusResponse;
import com.tikitaka.backend.space.dto.SpaceMemberSummaryResponse;
import com.tikitaka.backend.space.dto.SpaceSummaryResponse;
import com.tikitaka.backend.space.entity.Schedule;
import com.tikitaka.backend.space.entity.Space;
import com.tikitaka.backend.space.entity.SpaceMember;
import com.tikitaka.backend.space.repository.ScheduleRepository;
import com.tikitaka.backend.space.repository.SpaceMemberRepository;
import com.tikitaka.backend.space.repository.SpaceRepository;
import com.tikitaka.backend.user.entity.Role;
import com.tikitaka.backend.user.entity.User;
import com.tikitaka.backend.user.repository.UserRepository;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

import com.tikitaka.backend.space.dto.CreateDocumentResponse;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
@Hidden
public class SpaceService {

    private static final String DEFAULT_TIMEZONE = "Asia/Seoul";
    private final DocumentRepository documentRepository;
    private final SlideRepository slideRepository;
    private final PdfSlideConvertService pdfSlideConvertService;
    private final S3StorageService s3StorageService;
    private static final int SPACE_CODE_LENGTH = 8;
    private static final String SPACE_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Set<String> ALLOWED_DAYS = Set.of(
        "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    );
    private static final Set<String> ALLOWED_MEMBER_VALIDITIES = Set.of("PENDING", "APPROVED", "DENIED");

    private final SpaceRepository spaceRepository;
    private final ScheduleRepository scheduleRepository;
    private final SpaceMemberRepository spaceMemberRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SpaceSummaryResponse> getMySpaces(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        return spaceMemberRepository.findApprovedSpacesByUserId(user.getId());
    }

    public CreateSpaceResponse createSpace(UUID professorId, CreateSpaceRequest request) {
        User professor = userRepository.findById(professorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (professor.getRole() != Role.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "교수만 강의를 생성할 수 있습니다.");
        }

        Space space = spaceRepository.save(Space.builder()
            .professor(professor)
            .name(request.name())
            .semester(request.semester())
            .spaceCode(generateUniqueSpaceCode())
            .color(request.color())
            .build());

        for (CreateScheduleRequest scheduleRequest : request.schedules()) {
            String day = scheduleRequest.day().toUpperCase(Locale.ROOT);
            if (!ALLOWED_DAYS.contains(day)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요일 형식이 올바르지 않습니다.");
            }

            LocalTime startTime = parseTime(scheduleRequest.startTime(), "시작 시간 형식이 올바르지 않습니다.");
            LocalTime endTime = parseTime(scheduleRequest.endTime(), "종료 시간 형식이 올바르지 않습니다.");

            if (!startTime.isBefore(endTime)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작 시간은 종료 시간보다 빨라야 합니다.");
            }

            scheduleRepository.save(Schedule.builder()
                .space(space)
                .day(day)
                .startTime(startTime)
                .endTime(endTime)
                .timezone(DEFAULT_TIMEZONE)
                .build());
        }

        spaceMemberRepository.save(SpaceMember.builder()
            .space(space)
            .user(professor)
            .validity("APPROVED")
            .nickname(request.nickname())
            .approvedAt(OffsetDateTime.now(ZoneOffset.UTC))
            .lastAccessedAt(OffsetDateTime.now(ZoneOffset.UTC))
            .build());

        return new CreateSpaceResponse(
            space.getId(),
            space.getName(),
            request.nickname(),
            space.getSemester(),
            space.getColor(),
            space.getSpaceCode()
        );
    }

    public JoinSpaceResponse joinSpace(UUID studentId, JoinSpaceRequest request) {
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (student.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생만 강의 참여를 요청할 수 있습니다.");
        }

        String normalizedCode = normalizeSpaceCode(request.spaceCode());

        Space space = spaceRepository.findBySpaceCode(normalizedCode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "초대 코드에 해당하는 강의를 찾을 수 없습니다."));

        if (spaceMemberRepository.findBySpaceIdAndUserId(space.getId(), student.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 참여 중이거나 참여 요청한 강의입니다.");
        }

        SpaceMember spaceMember = spaceMemberRepository.save(SpaceMember.builder()
            .space(space)
            .user(student)
            .validity("PENDING")
            .build());

        return new JoinSpaceResponse(
            spaceMember.getId(),
            space.getId(),
            space.getName(),
            spaceMember.getValidity()
        );
    }

    @Transactional(readOnly = true)
    public SpaceLookupResponse lookupSpaceByCode(UUID studentId, String spaceCode) {
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (student.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "학생만 강의 조회를 요청할 수 있습니다.");
        }

        String normalizedCode = normalizeSpaceCode(spaceCode);

        Space space = spaceRepository.findBySpaceCode(normalizedCode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 초대 코드의 강의를 찾을 수 없습니다."));

        String membershipStatus = spaceMemberRepository.findBySpaceIdAndUserId(space.getId(), student.getId())
            .map(SpaceMember::getValidity)
            .orElse(null);

        if (membershipStatus != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 참여 중이거나 참여 요청 대기 중인 강의입니다.");
        }

        String nickname = spaceMemberRepository.findBySpaceIdAndUserId(space.getId(), space.getProfessor().getId())
            .map(SpaceMember::getNickname)
            .orElse(null);

        List<SpaceLookupScheduleResponse> schedules = scheduleRepository
            .findBySpaceIdOrderByDayAscStartTimeAsc(space.getId())
            .stream()
            .map(schedule -> new SpaceLookupScheduleResponse(
                schedule.getDay(),
                schedule.getStartTime().toString(),
                schedule.getEndTime().toString()
            ))
            .toList();

        return new SpaceLookupResponse(
            space.getId(),
            space.getSpaceCode(),
            space.getName(),
            nickname,
            space.getSemester(),
            space.getProfessor().getName(),
            schedules
        );
    }

    @Transactional(readOnly = true)
    public List<SpaceMemberSummaryResponse> getSpaceMembers(UUID professorId, UUID spaceId, String validity) {
        User professor = userRepository.findById(professorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (professor.getRole() != Role.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "교수만 강의 참여자 목록을 조회할 수 있습니다.");
        }

        String normalizedValidity = validity.toUpperCase(Locale.ROOT);
        if (!ALLOWED_MEMBER_VALIDITIES.contains(normalizedValidity)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 참여 상태입니다.");
        }

        Space space = spaceRepository.findById(spaceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다."));

        if (!space.getProfessor().getId().equals(professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 생성한 강의의 참여자 목록만 조회할 수 있습니다.");
        }

        return spaceMemberRepository.findMembersBySpaceIdAndValidity(spaceId, normalizedValidity);
    }

    public SpaceMemberStatusResponse approveSpaceMember(UUID professorId, UUID spaceId, UUID memberId) {
        SpaceMember member = getProfessorOwnedSpaceMember(professorId, spaceId, memberId);
        member.approve(OffsetDateTime.now(ZoneOffset.UTC));

        return new SpaceMemberStatusResponse(
            member.getId(),
            member.getSpace().getId(),
            member.getUser().getId(),
            member.getValidity()
        );
    }

    public SpaceMemberStatusResponse denySpaceMember(UUID professorId, UUID spaceId, UUID memberId) {
        SpaceMember member = getProfessorOwnedSpaceMember(professorId, spaceId, memberId);
        member.deny(OffsetDateTime.now(ZoneOffset.UTC));

        return new SpaceMemberStatusResponse(
            member.getId(),
            member.getSpace().getId(),
            member.getUser().getId(),
            member.getValidity()
        );
    }

    public void recordSpaceAccess(UUID userId, UUID spaceId) {
        SpaceMember member = spaceMemberRepository.findBySpaceIdAndUserId(spaceId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Space membership not found."
            ));

        if (!"APPROVED".equals(member.getValidity())) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only approved space members can record access."
            );
        }

        member.recordAccess(OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Transactional(readOnly = true)
    public SpaceCodeResponse getSpaceCode(UUID professorId, UUID spaceId) {
        User professor = userRepository.findById(professorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (professor.getRole() != Role.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "교수만 강의 초대 코드를 조회할 수 있습니다.");
        }

        Space space = spaceRepository.findById(spaceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다."));

        if (!space.getProfessor().getId().equals(professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 생성한 강의의 초대 코드만 조회할 수 있습니다.");
        }

        return new SpaceCodeResponse(space.getId(), space.getSpaceCode());
    }

    private SpaceMember getProfessorOwnedSpaceMember(UUID professorId, UUID spaceId, UUID memberId) {
        User professor = userRepository.findById(professorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (professor.getRole() != Role.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "교수만 학생 참여 요청을 처리할 수 있습니다.");
        }

        Space space = spaceRepository.findById(spaceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다."));

        if (!space.getProfessor().getId().equals(professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 생성한 강의의 참여 요청만 처리할 수 있습니다.");
        }

        return spaceMemberRepository.findByIdAndSpaceId(memberId, spaceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "강의 참여 요청을 찾을 수 없습니다."));
    }

    private String generateUniqueSpaceCode() {
        String candidate;
        do {
            candidate = randomSpaceCode();
        } while (spaceRepository.existsBySpaceCode(candidate));
        return candidate;
    }

    private String randomSpaceCode() {
        StringBuilder builder = new StringBuilder(SPACE_CODE_LENGTH);
        for (int i = 0; i < SPACE_CODE_LENGTH; i++) {
            int index = ThreadLocalRandom.current().nextInt(SPACE_CODE_CHARACTERS.length());
            builder.append(SPACE_CODE_CHARACTERS.charAt(index));
        }
        return builder.toString();
    }

    private String normalizeSpaceCode(String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "강의 초대 코드는 필수입니다.");
        }

        String normalizedValue = value.trim().toUpperCase(Locale.ROOT);
        if (normalizedValue.length() != SPACE_CODE_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "강의 초대 코드 형식이 올바르지 않습니다.");
        }

        return normalizedValue;
    }

    private LocalTime parseTime(String value, String message) {
        try {
            return LocalTime.parse(value);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentSummaryResponse> getDocuments(UUID userId, UUID spaceId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        if (!spaceRepository.existsById(spaceId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다.");
        }

        boolean isApprovedMember = spaceMemberRepository.existsBySpaceIdAndUserIdAndValidity(
                spaceId,
                userId,
                "APPROVED"
        );

        if (!isApprovedMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "강의 참여자만 강의자료를 조회할 수 있습니다.");
        }

        return documentRepository.findBySpaceIdOrderByCreatedAtDesc(spaceId)
                .stream()
                .map(this::toDocumentSummaryResponse)
                .toList();
    }

    private DocumentSummaryResponse toDocumentSummaryResponse(Document document) {
        return new DocumentSummaryResponse(
                document.getId(),
                document.getTitle(),
                document.getThumbnailUrl(),
                document.getPdfUrl(),
                document.getCreatedAt().toLocalDate()
        );
    }

    public CreateDocumentResponse createDocument(UUID professorId, UUID spaceId, String title, MultipartFile file) {
        User professor = userRepository.findById(professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (professor.getRole() != Role.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "교수만 강의자료를 등록할 수 있습니다.");
        }

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다."));

        if (!space.getProfessor().getId().equals(professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 생성한 강의에만 강의자료를 등록할 수 있습니다.");
        }

        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "강의자료 제목은 필수입니다.");
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
            tempPdfPath = Files.createTempFile("lecture-" + documentFileKey + "-", ".pdf");
            Files.copy(file.getInputStream(), tempPdfPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String pdfUrl = s3StorageService.uploadLecturePdf(
                    documentFileKey,
                    originalFilename,
                    file
            );

            PdfSlideConvertResult convertResult = pdfSlideConvertService.convertPdfToSlideImages(
                    tempPdfPath,
                    documentFileKey
            );

            Document document = documentRepository.save(Document.builder()
                    .space(space)
                    .title(title)
                    .thumbnailUrl(convertResult.thumbnailUrl())
                    .pdfUrl(pdfUrl)
                    .pdfPageCount(convertResult.pageCount())
                    .version(1)
                    .build());

            for (int i = 0; i < convertResult.slideImageUrls().size(); i++) {
                Slide slide = Slide.builder()
                        .document(document)
                        .pageNumber(i + 1)
                        .version(1)
                        .imageUrl(convertResult.slideImageUrls().get(i))
                        .isReplaced(false)
                        .isDeleted(false)
                        .build();

                slideRepository.save(slide);
            }

            return new CreateDocumentResponse(
                    document.getId(),
                    space.getId(),
                    document.getTitle(),
                    document.getPdfUrl(),
                    document.getThumbnailUrl()
            );
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "강의자료 등록 중 오류가 발생했습니다."
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
