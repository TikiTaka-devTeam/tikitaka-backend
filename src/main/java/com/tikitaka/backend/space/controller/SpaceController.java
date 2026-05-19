package com.tikitaka.backend.space.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tikitaka.backend.global.jwt.JwtProvider;
import com.tikitaka.backend.space.dto.CreateSpaceRequest;
import com.tikitaka.backend.space.dto.CreateSpaceResponse;
import com.tikitaka.backend.space.dto.SpaceSummaryResponse;
import com.tikitaka.backend.space.service.SpaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/spaces")
@RequiredArgsConstructor
@Tag(name = "Space", description = "강의 생성, 조회, 참여 요청 등 강의 공간 관련 API")
public class SpaceController {

    private final SpaceService spaceService;
    private final JwtProvider jwtProvider;

    @GetMapping
    @Operation(
        summary = "내 강의 목록 조회",
        description = "현재 로그인한 사용자가 승인된 상태로 참여 중인 강의 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "강의 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = SpaceSummaryResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
        @ApiResponse(responseCode = "404", description = "요청 사용자를 찾을 수 없음")
    })
    public ResponseEntity<List<SpaceSummaryResponse>> getMySpaces(
        @Parameter(
            description = "Bearer 액세스 토큰",
            example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
        )
        @RequestHeader("Authorization") String authHeader
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID userId = UUID.fromString(jwtProvider.extractUserId(accessToken));
        List<SpaceSummaryResponse> response = spaceService.getMySpaces(userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(
        summary = "강의 생성",
        description = "교수 사용자가 강의를 생성하고 초대 코드를 발급합니다. 요청에 포함된 시간표도 함께 저장됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "강의 생성 성공",
            content = @Content(schema = @Schema(implementation = CreateSpaceResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 값"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
        @ApiResponse(responseCode = "403", description = "교수 권한이 아닌 사용자의 요청"),
        @ApiResponse(responseCode = "404", description = "요청 사용자를 찾을 수 없음")
    })
    public ResponseEntity<CreateSpaceResponse> createSpace(
        @Parameter(
            description = "Bearer 액세스 토큰",
            example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
        )
        @RequestHeader("Authorization") String authHeader,
        @RequestBody @Valid CreateSpaceRequest request
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID professorId = UUID.fromString(jwtProvider.extractUserId(accessToken));
        CreateSpaceResponse response = spaceService.createSpace(professorId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization 헤더가 올바르지 않습니다.");
        }
        return authHeader.substring(7);
    }
}
