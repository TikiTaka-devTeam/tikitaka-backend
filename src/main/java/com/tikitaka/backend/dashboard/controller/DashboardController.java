package com.tikitaka.backend.dashboard.controller;

import com.tikitaka.backend.dashboard.dto.NextSpaceResponse;
import com.tikitaka.backend.dashboard.dto.RecentSpaceResponse;
import com.tikitaka.backend.dashboard.dto.ScheduleResponse;
import com.tikitaka.backend.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Dashboard", description = "대시보드 API")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/recent-spaces")
    @Operation(summary = "최근 접속한 강의 조회", description = "최근 접속한 강의 2개를 조회합니다.")
    public ResponseEntity<List<RecentSpaceResponse>> getRecentSpaces() {
        UUID userId = UUID.fromString(
            (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return ResponseEntity.ok(dashboardService.getRecentSpaces(userId));
    }

    @GetMapping("/schedules")
    @Operation(summary = "시간표 조회", description = "내 강의 시간표를 조회합니다.")
    public ResponseEntity<List<ScheduleResponse>> getSchedules() {
        UUID userId = UUID.fromString(
            (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return ResponseEntity.ok(dashboardService.getSchedules(userId));
    }

    @GetMapping("/next-space")
    @Operation(summary = "다음 강의 조회", description = "현재 시간 기준 다음 강의를 조회합니다.")
    public ResponseEntity<NextSpaceResponse> getNextSpace() {
        UUID userId = UUID.fromString(
            (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        NextSpaceResponse response = dashboardService.getNextSpace(userId);
        if (response == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(response);
    }
}