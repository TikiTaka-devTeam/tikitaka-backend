package com.tikitaka.backend.dashboard.controller;

import com.tikitaka.backend.dashboard.dto.NextSpaceResponse;
import com.tikitaka.backend.dashboard.dto.RecentSpaceResponse;
import com.tikitaka.backend.dashboard.dto.ScheduleResponse;
import com.tikitaka.backend.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 최근 접속한 강의 조회
    @GetMapping("/recent-spaces")
    public ResponseEntity<List<RecentSpaceResponse>> getRecentSpaces() {
        UUID userId = UUID.fromString(
            (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return ResponseEntity.ok(dashboardService.getRecentSpaces(userId));
    }

    // 시간표 조회
    @GetMapping("/schedules")
    public ResponseEntity<List<ScheduleResponse>> getSchedules() {
        UUID userId = UUID.fromString(
            (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return ResponseEntity.ok(dashboardService.getSchedules(userId));
    }

    // 다음 강의 조회
    @GetMapping("/next-space")
    public ResponseEntity<NextSpaceResponse> getNextSpace() {
        UUID userId = UUID.fromString(
            (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        NextSpaceResponse response = dashboardService.getNextSpace(userId);
        if (response == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(response);
    }
}