package com.tikitaka.backend.space.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.tikitaka.backend.space.dto.DocumentSummaryResponse;
import com.tikitaka.backend.space.dto.CreateDocumentResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import com.tikitaka.backend.global.config.SwaggerConfig;
import com.tikitaka.backend.global.jwt.JwtProvider;
import com.tikitaka.backend.space.dto.SpaceMemberStatusResponse;
import com.tikitaka.backend.space.dto.SpaceMemberSummaryResponse;
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
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/spaces")
@RequiredArgsConstructor
@Tag(name = "My Space", description = "마이 스페이스 관련 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_SCHEME_NAME)
public class MySpaceController {

    private final SpaceService spaceService;
    private final JwtProvider jwtProvider;

    @PostMapping("/{space_id}/access")
    @Operation(
        summary = "최근 Space 업데이트",
        description = "Updates the approved member's last access time for the space."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Space access recorded"),
        @ApiResponse(responseCode = "401", description = "Invalid access token"),
        @ApiResponse(responseCode = "403", description = "Space membership is not approved"),
        @ApiResponse(responseCode = "404", description = "Space membership not found")
    })
    public ResponseEntity<Void> recordSpaceAccess(
        @Parameter(hidden = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Space ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable("space_id") UUID spaceId
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID userId = UUID.fromString(jwtProvider.extractUserId(accessToken));
        spaceService.recordSpaceAccess(userId, spaceId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{space_id}/members")
    @Operation(
        summary = "강의 참여자 목록 조회",
        description = "교수 사용자가 강의의 참여 요청 대기 또는 승인된 학생 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "강의 참여자 목록 조회 성공",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = SpaceMemberSummaryResponse.class))
            )
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 참여 상태 값"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
        @ApiResponse(responseCode = "403", description = "교수 권한이 아니거나 본인 강의가 아닌 경우"),
        @ApiResponse(responseCode = "404", description = "요청 사용자 또는 강의를 찾을 수 없음")
    })
    public ResponseEntity<List<SpaceMemberSummaryResponse>> getSpaceMembers(
        @Parameter(hidden = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "강의 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable("space_id") UUID spaceId,
        @Parameter(description = "참여 상태", example = "PENDING")
        @RequestParam("validity") String validity
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID professorId = UUID.fromString(jwtProvider.extractUserId(accessToken));
        List<SpaceMemberSummaryResponse> response = spaceService.getSpaceMembers(professorId, spaceId, validity);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{space_id}/members/{member_id}/approve")
    @Operation(
        summary = "학생 참여 요청 승인",
        description = "교수 사용자가 학생의 강의 참여 요청을 승인합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "학생 참여 요청 승인 성공",
            content = @Content(schema = @Schema(implementation = SpaceMemberStatusResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
        @ApiResponse(responseCode = "403", description = "교수 권한이 아니거나 본인 강의가 아닌 경우"),
        @ApiResponse(responseCode = "404", description = "요청 사용자, 강의 또는 참여 요청을 찾을 수 없음")
    })
    public ResponseEntity<SpaceMemberStatusResponse> approveSpaceMember(
        @Parameter(hidden = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "강의 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable("space_id") UUID spaceId,
        @Parameter(description = "강의 멤버 ID", example = "123e4567-e89b-12d3-a456-426614174111")
        @PathVariable("member_id") UUID memberId
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID professorId = UUID.fromString(jwtProvider.extractUserId(accessToken));
        SpaceMemberStatusResponse response = spaceService.approveSpaceMember(professorId, spaceId, memberId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{space_id}/members/{member_id}/deny")
    @Operation(
        summary = "학생 참여 요청 거절",
        description = "교수 사용자가 학생의 강의 참여 요청을 거절합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "학생 참여 요청 거절 성공",
            content = @Content(schema = @Schema(implementation = SpaceMemberStatusResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
        @ApiResponse(responseCode = "403", description = "교수 권한이 아니거나 본인 강의가 아닌 경우"),
        @ApiResponse(responseCode = "404", description = "요청 사용자, 강의 또는 참여 요청을 찾을 수 없음")
    })
    public ResponseEntity<SpaceMemberStatusResponse> denySpaceMember(
        @Parameter(hidden = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "강의 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable("space_id") UUID spaceId,
        @Parameter(description = "강의 멤버 ID", example = "123e4567-e89b-12d3-a456-426614174111")
        @PathVariable("member_id") UUID memberId
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID professorId = UUID.fromString(jwtProvider.extractUserId(accessToken));
        SpaceMemberStatusResponse response = spaceService.denySpaceMember(professorId, spaceId, memberId);

        return ResponseEntity.ok(response);
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization 헤더가 올바르지 않습니다.");
        }
        return authHeader.substring(7);
    }

    @GetMapping("/{space_id}/documents")
    @Operation(
            summary = "강의자료 목록 조회",
            description = "강의 참여자가 특정 강의의 강의자료 목록을 날짜순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "강의자료 목록 조회 성공",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = DocumentSummaryResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
            @ApiResponse(responseCode = "403", description = "강의 참여자가 아닌 경우"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 강의를 찾을 수 없음")
    })
    public ResponseEntity<List<DocumentSummaryResponse>> getDocuments(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader,

            @Parameter(description = "강의 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("space_id") UUID spaceId
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID userId = UUID.fromString(jwtProvider.extractUserId(accessToken));

        List<DocumentSummaryResponse> response = spaceService.getDocuments(userId, spaceId);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{space_id}/documents", consumes = "multipart/form-data")
    @Operation(
            summary = "새로운 강의자료 등록",
            description = "교수 사용자가 본인이 생성한 강의에 새로운 PDF 강의자료를 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "강의자료 등록 성공",
                    content = @Content(schema = @Schema(implementation = CreateDocumentResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 PDF가 아닌 파일"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
            @ApiResponse(responseCode = "403", description = "교수 권한이 아니거나 본인 강의가 아닌 경우"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 강의를 찾을 수 없음")
    })
    public ResponseEntity<CreateDocumentResponse> createDocument(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader,

            @Parameter(description = "강의 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("space_id") UUID spaceId,

            @Parameter(description = "강의자료 제목", example = "운영체제 1주차")
            @RequestPart("title") String title,

            @Parameter(description = "PDF 파일")
            @RequestPart("file") MultipartFile file
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID professorId = UUID.fromString(jwtProvider.extractUserId(accessToken));

        CreateDocumentResponse response = spaceService.createDocument(
                professorId,
                spaceId,
                title,
                file
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
