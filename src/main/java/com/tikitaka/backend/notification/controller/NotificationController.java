package com.tikitaka.backend.notification.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tikitaka.backend.global.config.SwaggerConfig;
import com.tikitaka.backend.global.config.security.CurrentUserProvider;
import com.tikitaka.backend.notification.dto.NotificationReadResponse;
import com.tikitaka.backend.notification.dto.NotificationResponse;
import com.tikitaka.backend.notification.dto.NotificationTargetResponse;
import com.tikitaka.backend.notification.service.NotificationService;
import com.tikitaka.backend.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_SCHEME_NAME)
public class NotificationController {

    private final CurrentUserProvider currentUserProvider;
    private final NotificationService notificationService;

    @GetMapping
    @Operation(
        summary = "내 알림 목록 최신순 조회",
        description = "현재 로그인한 사용자의 알림 목록을 최신순으로 조회합니다. is_read 파라미터로 읽음 여부를 필터링할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "내 알림 목록 조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationResponse.class)))
        ),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰")
    })
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
        @RequestParam(name = "is_read", required = false) Boolean isRead
    ) {
        User currentUser = currentUserProvider.getCurrentUser();

        return ResponseEntity.ok(notificationService.getMyNotifications(currentUser.getId(), isRead));
    }

    @PatchMapping("/{notification_id}/read")
    @Operation(
        summary = "알림 읽음 처리",
        description = "현재 로그인한 사용자의 알림을 읽음 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "알림 읽음 처리 성공",
            content = @Content(schema = @Schema(implementation = NotificationReadResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
        @ApiResponse(responseCode = "403", description = "알림 접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    public ResponseEntity<NotificationReadResponse> markAsRead(
        @PathVariable("notification_id") UUID notificationId
    ) {
        User currentUser = currentUserProvider.getCurrentUser();

        return ResponseEntity.ok(notificationService.markAsRead(currentUser.getId(), notificationId));
    }

    @GetMapping("/{notification_id}/target")
    @Operation(
        summary = "알림 클릭 시 이동 대상 조회",
        description = "현재 로그인한 사용자의 알림 이동 대상 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "알림 이동 대상 조회 성공",
            content = @Content(schema = @Schema(implementation = NotificationTargetResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
        @ApiResponse(responseCode = "403", description = "알림 접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    public ResponseEntity<NotificationTargetResponse> getNotificationTarget(
        @PathVariable("notification_id") UUID notificationId
    ) {
        User currentUser = currentUserProvider.getCurrentUser();

        return ResponseEntity.ok(notificationService.getNotificationTarget(currentUser.getId(), notificationId));
    }
}
