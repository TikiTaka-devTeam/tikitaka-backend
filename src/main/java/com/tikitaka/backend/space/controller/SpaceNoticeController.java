package com.tikitaka.backend.space.controller;

import com.tikitaka.backend.global.config.SwaggerConfig;
import com.tikitaka.backend.global.jwt.JwtProvider;
import com.tikitaka.backend.space.dto.CreateSpaceNoticeRequest;
import com.tikitaka.backend.space.dto.DeleteSpaceNoticeResponse;
import com.tikitaka.backend.space.dto.SpaceNoticeResponse;
import com.tikitaka.backend.space.dto.UpdateSpaceNoticeRequest;
import com.tikitaka.backend.space.service.SpaceNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Space Notice", description = "스페이스 공지 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_SCHEME_NAME)
public class SpaceNoticeController {

    private final SpaceNoticeService spaceNoticeService;
    private final JwtProvider jwtProvider;

    @GetMapping("/api/v1/spaces/{space_id}/notices")
    @Operation(
            summary = "스페이스 공지 목록 조회",
            description = "강의 참여자 또는 담당 교수가 특정 스페이스의 공지 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공지 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SpaceNoticeResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
            @ApiResponse(responseCode = "403", description = "강의 참여자가 아닌 경우"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 강의를 찾을 수 없음")
    })
    public ResponseEntity<List<SpaceNoticeResponse>> getNotices(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader,

            @Parameter(description = "강의 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("space_id") UUID spaceId
    ) {
        UUID userId = extractUserId(authHeader);
        List<SpaceNoticeResponse> response = spaceNoticeService.getNotices(userId, spaceId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/spaces/{space_id}/notices")
    @Operation(
            summary = "스페이스 공지 생성",
            description = "교수가 본인이 생성한 스페이스에 공지를 생성합니다. 생성 시 승인된 학생들에게 알림이 생성됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "공지 생성 성공",
                    content = @Content(schema = @Schema(implementation = SpaceNoticeResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
            @ApiResponse(responseCode = "403", description = "교수 권한이 아니거나 본인 강의가 아닌 경우"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 강의를 찾을 수 없음")
    })
    public ResponseEntity<SpaceNoticeResponse> createNotice(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader,

            @Parameter(description = "강의 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("space_id") UUID spaceId,

            @Valid @RequestBody CreateSpaceNoticeRequest request
    ) {
        UUID professorId = extractUserId(authHeader);
        SpaceNoticeResponse response = spaceNoticeService.createNotice(professorId, spaceId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/api/v1/notices/{notice_id}")
    @Operation(
            summary = "공지 수정",
            description = "교수가 본인 강의의 공지를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공지 수정 성공",
                    content = @Content(schema = @Schema(implementation = SpaceNoticeResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
            @ApiResponse(responseCode = "403", description = "교수 권한이 아니거나 본인 강의가 아닌 경우"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 공지를 찾을 수 없음")
    })
    public ResponseEntity<SpaceNoticeResponse> updateNotice(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader,

            @Parameter(description = "공지 ID", example = "123e4567-e89b-12d3-a456-426614174999")
            @PathVariable("notice_id") UUID noticeId,

            @Valid @RequestBody UpdateSpaceNoticeRequest request
    ) {
        UUID professorId = extractUserId(authHeader);
        SpaceNoticeResponse response = spaceNoticeService.updateNotice(professorId, noticeId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/v1/notices/{notice_id}")
    @Operation(
            summary = "공지 삭제",
            description = "교수가 본인 강의의 공지를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공지 삭제 성공",
                    content = @Content(schema = @Schema(implementation = DeleteSpaceNoticeResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
            @ApiResponse(responseCode = "403", description = "교수 권한이 아니거나 본인 강의가 아닌 경우"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 공지를 찾을 수 없음")
    })
    public ResponseEntity<DeleteSpaceNoticeResponse> deleteNotice(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader,

            @Parameter(description = "공지 ID", example = "123e4567-e89b-12d3-a456-426614174999")
            @PathVariable("notice_id") UUID noticeId
    ) {
        UUID professorId = extractUserId(authHeader);
        DeleteSpaceNoticeResponse response = spaceNoticeService.deleteNotice(professorId, noticeId);

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