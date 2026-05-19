package com.tikitaka.backend.space.service;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tikitaka.backend.space.dto.CreateScheduleRequest;
import com.tikitaka.backend.space.dto.CreateSpaceRequest;
import com.tikitaka.backend.space.dto.CreateSpaceResponse;
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

@Service
@RequiredArgsConstructor
@Transactional
@Hidden
public class SpaceService {

    private static final String DEFAULT_TIMEZONE = "Asia/Seoul";
    private static final int SPACE_CODE_LENGTH = 8;
    private static final String SPACE_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Set<String> ALLOWED_DAYS = Set.of(
        "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    );

    private final SpaceRepository spaceRepository;
    private final ScheduleRepository scheduleRepository;
    private final SpaceMemberRepository spaceMemberRepository;
    private final UserRepository userRepository;

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

    private LocalTime parseTime(String value, String message) {
        try {
            return LocalTime.parse(value);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
