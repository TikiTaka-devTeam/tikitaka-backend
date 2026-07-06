package com.tikitaka.backend.revision.controller;

import com.tikitaka.backend.global.config.SwaggerConfig;
import com.tikitaka.backend.global.jwt.JwtProvider;
import com.tikitaka.backend.revision.dto.CreateDocumentReplacementResponse;
import com.tikitaka.backend.revision.service.DocumentRevisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Document Revision API", description = "강의자료 수정용 임시 업로드 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_SCHEME_NAME)
public class DocumentRevisionController {

    private final DocumentRevisionService documentRevisionService;
    private final JwtProvider jwtProvider;

    @PostMapping(value = "/documents/{document_id}/replacement", consumes = "multipart/form-data")
    @Operation(
            summary = "강의자료 수정을 위한 새로운 PDF 임시 업로드",
            description = "교수 사용자가 기존 강의자료를 수정하기 위해 새 PDF를 임시 업로드합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "수정용 PDF 임시 업로드 성공",
                    content = @Content(schema = @Schema(implementation = CreateDocumentReplacementResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 PDF가 아닌 파일"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
            @ApiResponse(responseCode = "403", description = "본인 강의자료가 아닌 경우"),
            @ApiResponse(responseCode = "404", description = "강의자료를 찾을 수 없음")
    })
    public ResponseEntity<CreateDocumentReplacementResponse> createReplacement(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader,

            @Parameter(description = "강의자료 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("document_id") UUID documentId,

            @Parameter(description = "새 PDF 파일")
            @RequestPart("file") MultipartFile file
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID professorId = UUID.fromString(jwtProvider.extractUserId(accessToken));

        CreateDocumentReplacementResponse response = documentRevisionService.createReplacement(
                professorId,
                documentId,
                file
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization 헤더가 올바르지 않습니다.");
        }

        return authHeader.substring(7);
    }
}
