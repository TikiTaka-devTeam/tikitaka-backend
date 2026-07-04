package com.tikitaka.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tikitaka.backend.document.repository.DocumentRepository;
import com.tikitaka.backend.global.storage.S3StorageService;
import com.tikitaka.backend.slide.repository.SlideRepository;
import com.tikitaka.backend.slide.service.PdfSlideConvertService;
import com.tikitaka.backend.space.entity.SpaceMember;
import com.tikitaka.backend.space.repository.ScheduleRepository;
import com.tikitaka.backend.space.repository.SpaceMemberRepository;
import com.tikitaka.backend.space.repository.SpaceRepository;
import com.tikitaka.backend.space.service.SpaceService;
import com.tikitaka.backend.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SpaceServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private SlideRepository slideRepository;

    @Mock
    private PdfSlideConvertService pdfSlideConvertService;

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private SpaceMemberRepository spaceMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SpaceService spaceService;

    @Test
    void recordSpaceAccessUpdatesApprovedMember() {
        UUID userId = UUID.randomUUID();
        UUID spaceId = UUID.randomUUID();
        SpaceMember member = SpaceMember.builder()
            .validity("APPROVED")
            .build();

        when(spaceMemberRepository.findBySpaceIdAndUserId(spaceId, userId))
            .thenReturn(Optional.of(member));

        spaceService.recordSpaceAccess(userId, spaceId);

        assertNotNull(member.getLastAccessedAt());
    }

    @Test
    void recordSpaceAccessRejectsUnapprovedMember() {
        UUID userId = UUID.randomUUID();
        UUID spaceId = UUID.randomUUID();
        SpaceMember member = SpaceMember.builder()
            .validity("PENDING")
            .build();

        when(spaceMemberRepository.findBySpaceIdAndUserId(spaceId, userId))
            .thenReturn(Optional.of(member));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> spaceService.recordSpaceAccess(userId, spaceId)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertNull(member.getLastAccessedAt());
    }

    @Test
    void recordSpaceAccessRejectsMissingMembership() {
        UUID userId = UUID.randomUUID();
        UUID spaceId = UUID.randomUUID();

        when(spaceMemberRepository.findBySpaceIdAndUserId(spaceId, userId))
            .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> spaceService.recordSpaceAccess(userId, spaceId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
