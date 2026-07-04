package com.tikitaka.backend.notification.controller;

import com.tikitaka.backend.global.config.SwaggerConfig;
import com.tikitaka.backend.global.jwt.JwtProvider;
import com.tikitaka.backend.notification.dto.NotificationCountResponse;
import com.tikitaka.backend.notification.dto.NotificationReadResponse;
import com.tikitaka.backend.notification.dto.NotificationResponse;
import com.tikitaka.backend.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "메인 대시보드 알림 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_SCHEME_NAME)
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtProvider jwtProvider;

    @GetMapping
    @Operation(
            summary = "내 알림 목록 조회",
            description = """
                    메인 대시보드 알림 목록을 조회합니다.
                    read 파라미터를 생략하면 전체, true면 읽음, false면 안 읽음 알림만 조회합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader,

            @Parameter(description = "읽음 여부 필터. 생략 시 전체", example = "false")
            @RequestParam(value = "read", required = false) Boolean read
    ) {
        UUID userId = extractUserId(authHeader);
        List<NotificationResponse> response = notificationService.getMyNotifications(userId, read);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/counts")
    @Operation(
            summary = "내 알림 개수 조회",
            description = "전체 / 읽음 / 안 읽음 알림 개수를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 개수 조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationCountResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<NotificationCountResponse> getMyNotificationCounts(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader
    ) {
        UUID userId = extractUserId(authHeader);
        NotificationCountResponse response = notificationService.getMyNotificationCounts(userId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{notification_id}/read")
    @Operation(
            summary = "알림 읽음 처리",
            description = "특정 알림을 읽음 상태로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 읽음 처리 성공",
                    content = @Content(schema = @Schema(implementation = NotificationReadResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
            @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    public ResponseEntity<NotificationReadResponse> readNotification(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader,

            @Parameter(description = "알림 ID", example = "123e4567-e89b-12d3-a456-426614174888")
            @PathVariable("notification_id") UUID notificationId
    ) {
        UUID userId = extractUserId(authHeader);
        NotificationReadResponse response = notificationService.readNotification(userId, notificationId);

        return ResponseEntity.ok(response);
    }

    private UUID extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization 헤더가 올바르지 않습니다.");
        }

        String accessToken = authHeader.substring(7);
        jwtProvider.isTokenValid(accessToken);

        return UUID.fromString(jwtProvider.extractUserId(accessToken));
    }
}