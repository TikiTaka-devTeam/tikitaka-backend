package com.tikitaka.backend.space.service;

import com.tikitaka.backend.notification.service.NotificationService;
import com.tikitaka.backend.space.dto.CreateSpaceNoticeRequest;
import com.tikitaka.backend.space.dto.DeleteSpaceNoticeResponse;
import com.tikitaka.backend.space.dto.SpaceNoticeResponse;
import com.tikitaka.backend.space.dto.UpdateSpaceNoticeRequest;
import com.tikitaka.backend.space.entity.Space;
import com.tikitaka.backend.space.entity.SpaceNotice;
import com.tikitaka.backend.space.repository.SpaceMemberRepository;
import com.tikitaka.backend.space.repository.SpaceNoticeRepository;
import com.tikitaka.backend.space.repository.SpaceRepository;
import com.tikitaka.backend.user.entity.Role;
import com.tikitaka.backend.user.entity.User;
import com.tikitaka.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SpaceNoticeService {

    private final SpaceNoticeRepository spaceNoticeRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceMemberRepository spaceMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<SpaceNoticeResponse> getNotices(UUID userId, UUID spaceId) {
        validateSpaceParticipant(userId, spaceId);

        return spaceNoticeRepository.findBySpaceIdOrderByIsPinnedDescCreatedAtDesc(spaceId)
                .stream()
                .map(SpaceNoticeResponse::from)
                .toList();
    }

    public SpaceNoticeResponse createNotice(UUID professorId, UUID spaceId, CreateSpaceNoticeRequest request) {
        User professor = userRepository.findById(professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (professor.getRole() != Role.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "교수만 공지를 생성할 수 있습니다.");
        }

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다."));

        if (!space.getProfessor().getId().equals(professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 생성한 강의에만 공지를 생성할 수 있습니다.");
        }

        SpaceNotice notice = spaceNoticeRepository.save(SpaceNotice.builder()
                .space(space)
                .professor(professor)
                .title(request.title())
                .content(request.content())
                .isPinned(request.isPinned() != null ? request.isPinned() : false)
                .build());

        List<User> approvedStudents = spaceMemberRepository.findApprovedStudentsBySpaceId(spaceId);
        notificationService.createSpaceNoticeNotifications(notice, approvedStudents);

        return SpaceNoticeResponse.from(notice);
    }

    public SpaceNoticeResponse updateNotice(UUID professorId, UUID noticeId, UpdateSpaceNoticeRequest request) {
        User professor = userRepository.findById(professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (professor.getRole() != Role.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "교수만 공지를 수정할 수 있습니다.");
        }

        SpaceNotice notice = spaceNoticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공지를 찾을 수 없습니다."));

        if (!notice.getSpace().getProfessor().getId().equals(professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 생성한 강의의 공지만 수정할 수 있습니다.");
        }

        notice.update(request.title(), request.content(), request.isPinned());

        return SpaceNoticeResponse.from(notice);
    }

    public DeleteSpaceNoticeResponse deleteNotice(UUID professorId, UUID noticeId) {
        User professor = userRepository.findById(professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (professor.getRole() != Role.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "교수만 공지를 삭제할 수 있습니다.");
        }

        SpaceNotice notice = spaceNoticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공지를 찾을 수 없습니다."));

        if (!notice.getSpace().getProfessor().getId().equals(professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 생성한 강의의 공지만 삭제할 수 있습니다.");
        }

        notificationService.deleteByNoticeId(notice);
        spaceNoticeRepository.delete(notice);

        return new DeleteSpaceNoticeResponse("공지가 삭제되었습니다.");
    }

    private void validateSpaceParticipant(UUID userId, UUID spaceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다."));

        boolean isProfessorOwner = user.getRole() == Role.PROFESSOR
                && space.getProfessor().getId().equals(userId);

        boolean isApprovedMember = spaceMemberRepository.existsBySpaceIdAndUserIdAndValidity(
                spaceId,
                userId,
                "APPROVED"
        );

        if (!isProfessorOwner && !isApprovedMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "강의 참여자만 공지를 조회할 수 있습니다.");
        }
    }
}