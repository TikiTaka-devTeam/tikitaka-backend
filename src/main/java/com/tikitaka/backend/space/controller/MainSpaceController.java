package com.tikitaka.backend.space.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tikitaka.backend.global.config.SwaggerConfig;
import com.tikitaka.backend.global.jwt.JwtProvider;
import com.tikitaka.backend.space.dto.CreateSpaceRequest;
import com.tikitaka.backend.space.dto.CreateSpaceResponse;
import com.tikitaka.backend.space.dto.JoinSpaceRequest;
import com.tikitaka.backend.space.dto.JoinSpaceResponse;
import com.tikitaka.backend.space.dto.SpaceCodeResponse;
import com.tikitaka.backend.space.dto.SpaceLookupResponse;
import com.tikitaka.backend.space.dto.SpaceSummaryResponse;
import com.tikitaka.backend.space.service.SpaceService;

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

@RestController
@RequestMapping("/api/v1/spaces")
@RequiredArgsConstructor
@Tag(name = "Main Space", description = "메인 스페이스 관련 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_SCHEME_NAME)
public class MainSpaceController {

    private final SpaceService spaceService;
    private final JwtProvider jwtProvider;

    @GetMapping
    @Operation(
        summary = "내가 속한 강의 목록 조회",
        description = "현재 로그인한 사용자가 승인된 상태로 참여 중인 강의 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "강의 목록 조회 성공",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = SpaceSummaryResponse.class))
            )
        ),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
        @ApiResponse(responseCode = "404", description = "요청 사용자를 찾을 수 없음")
    })
    public ResponseEntity<List<SpaceSummaryResponse>> getMySpaces(
        @Parameter(hidden = true)
        @RequestHeader("Authorization") String authHeader
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID userId = UUID.fromString(jwtProvider.extractUserId(accessToken));
        List<SpaceSummaryResponse> response = spaceService.getMySpaces(userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/lookup")
    @Operation(
        summary = "초대 코드로 강의 정보 조회",
        description = "학생 사용자가 초대 코드를 입력해 존재하는 강의인지 확인하고 기본 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "강의 조회 성공",
            content = @Content(schema = @Schema(implementation = SpaceLookupResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 초대 코드 형식"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
        @ApiResponse(responseCode = "403", description = "학생 권한이 아닌 사용자의 요청"),
        @ApiResponse(responseCode = "404", description = "해당 초대 코드의 강의를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "이미 참여 중이거나 참여 요청 대기 중인 강의")
    })
    public ResponseEntity<SpaceLookupResponse> lookupSpace(
        @Parameter(hidden = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "강의 초대 코드", example = "A1B2C3D4")
        @RequestParam("space_code") String spaceCode
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID studentId = UUID.fromString(jwtProvider.extractUserId(accessToken));
        SpaceLookupResponse response = spaceService.lookupSpaceByCode(studentId, spaceCode);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/join")
    @Operation(
        summary = "학생이 초대 코드로 강의 참여 요청",
        description = "학생 사용자가 강의 초대 코드로 강의 참여를 요청합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "강의 참여 요청 성공",
            content = @Content(schema = @Schema(implementation = JoinSpaceResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 값"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
        @ApiResponse(responseCode = "403", description = "학생 권한이 아닌 사용자의 요청"),
        @ApiResponse(responseCode = "404", description = "요청 사용자 또는 초대 코드에 해당하는 강의를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "이미 참여 중이거나 참여 요청한 강의")
    })
    public ResponseEntity<JoinSpaceResponse> joinSpace(
        @Parameter(hidden = true)
        @RequestHeader("Authorization") String authHeader,
        @RequestBody @Valid JoinSpaceRequest request
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID studentId = UUID.fromString(jwtProvider.extractUserId(accessToken));
        JoinSpaceResponse response = spaceService.joinSpace(studentId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{space_id}/code")
    @Operation(
        summary = "강의 초대 코드 조회",
        description = "교수 사용자가 자신이 생성한 강의의 초대 코드를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "강의 초대 코드 조회 성공",
            content = @Content(schema = @Schema(implementation = SpaceCodeResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
        @ApiResponse(responseCode = "403", description = "교수 권한이 아니거나 본인 강의가 아닌 경우"),
        @ApiResponse(responseCode = "404", description = "요청 사용자 또는 강의를 찾을 수 없음")
    })
    public ResponseEntity<SpaceCodeResponse> getSpaceCode(
        @Parameter(hidden = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "강의 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable("space_id") UUID spaceId
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID professorId = UUID.fromString(jwtProvider.extractUserId(accessToken));
        SpaceCodeResponse response = spaceService.getSpaceCode(professorId, spaceId);

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
        @Parameter(hidden = true)
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
