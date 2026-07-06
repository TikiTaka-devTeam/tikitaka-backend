package com.tikitaka.backend.slide.controller;

import com.tikitaka.backend.global.config.SwaggerConfig;
import com.tikitaka.backend.global.jwt.JwtProvider;
import com.tikitaka.backend.slide.dto.ReplaceSlideRequest;
import com.tikitaka.backend.slide.dto.ReplaceSlideResponse;
import com.tikitaka.backend.slide.service.SlideRevisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Slide Revision API", description = "강의자료 수정용 슬라이드 교체/삽입 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_SCHEME_NAME)
public class SlideRevisionController {

    private final SlideRevisionService slideRevisionService;
    private final JwtProvider jwtProvider;

    @PatchMapping("/slides/{slide_id}/replace")
    @Operation(
            summary = "기존 슬라이드를 수정 강의자료의 슬라이드로 교체 연결",
            description = "교수 사용자가 기존 슬라이드 하나와 수정 업로드된 슬라이드 하나를 연결합니다. 실제 반영은 revision complete 전까지 보류됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "슬라이드 교체 연결 성공",
                    content = @Content(schema = @Schema(implementation = ReplaceSlideResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청이거나 다른 강의자료의 수정 슬라이드 사용"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
            @ApiResponse(responseCode = "403", description = "본인 강의자료의 슬라이드가 아닌 경우"),
            @ApiResponse(responseCode = "404", description = "기존 슬라이드 또는 수정 슬라이드를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 다른 수정 슬라이드와 연결된 기존 슬라이드")
    })
    public ResponseEntity<ReplaceSlideResponse> replaceSlide(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader,

            @Parameter(description = "기존 슬라이드 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("slide_id") UUID slideId,

            @RequestBody @Valid ReplaceSlideRequest request
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID professorId = UUID.fromString(jwtProvider.extractUserId(accessToken));

        ReplaceSlideResponse response = slideRevisionService.replaceSlide(
                professorId,
                slideId,
                request.revisionSlideId()
        );

        return ResponseEntity.ok(response);
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization 헤더가 올바르지 않습니다.");
        }

        return authHeader.substring(7);
    }
}
