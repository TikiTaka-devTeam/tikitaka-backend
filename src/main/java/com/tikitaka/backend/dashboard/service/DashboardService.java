package com.tikitaka.backend.dashboard.service;

import com.tikitaka.backend.dashboard.dto.NextSpaceResponse;
import com.tikitaka.backend.dashboard.dto.RecentSpaceResponse;
import com.tikitaka.backend.dashboard.dto.ScheduleResponse;
import com.tikitaka.backend.dashboard.repository.DashboardRepository;
import com.tikitaka.backend.space.entity.SpaceMember;
import com.tikitaka.backend.space.entity.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    // 최근 접속한 강의 2개 조회
    public List<RecentSpaceResponse> getRecentSpaces(UUID userId) {
        return dashboardRepository.findRecentSpaces(userId).stream()
                .map(sm -> RecentSpaceResponse.builder()
                        .spaceId(sm.getSpace().getId())
                        .name(sm.getSpace().getName())
                        .nickname(sm.getNickname())
                        .color(sm.getSpace().getColor())
                        .professorName(sm.getSpace().getProfessor().getName())
                        .lastAccessedAt(sm.getLastAccessedAt())
                        .build())
                .toList();
    }

    // 시간표 조회
    public List<ScheduleResponse> getSchedules(UUID userId) {
        return dashboardRepository.findSchedulesByUserId(userId).stream()
                .map(sc -> ScheduleResponse.builder()
                        .spaceId(sc.getSpace().getId())
                        .spaceName(sc.getSpace().getName())
                        .day(sc.getDay())
                        .startTime(sc.getStartTime())
                        .endTime(sc.getEndTime())
                        .build())
                .toList();
    }

    // 다음 강의 조회
    public NextSpaceResponse getNextSpace(UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        String today = now.getDayOfWeek().name();
        LocalTime currentTime = now.toLocalTime();

        List<Schedule> result = dashboardRepository.findNextSchedule(userId, today, currentTime);

        if (result.isEmpty()) return null;

        Schedule sc = result.get(0);
        long remainTime = ChronoUnit.MINUTES.between(currentTime, sc.getStartTime());

        SpaceMember sm = dashboardRepository.findRecentSpaces(userId).stream()
                .filter(m -> m.getSpace().getId().equals(sc.getSpace().getId()))
                .findFirst()
                .orElse(null);

        return NextSpaceResponse.builder()
                .spaceId(sc.getSpace().getId())
                .nickname(sm != null ? sm.getNickname() : null)
                .spaceName(sc.getSpace().getName())
                .day(sc.getDay())
                .startTime(sc.getStartTime())
                .professorName(sc.getSpace().getProfessor().getName())
                .semester(sc.getSpace().getSemester())
                .remainTime(remainTime)
                .build();
    }
}