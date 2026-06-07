package com.tikitaka.backend.stroke.controller;

import com.tikitaka.backend.global.config.SwaggerConfig;
import com.tikitaka.backend.global.jwt.JwtProvider;
import com.tikitaka.backend.global.exception.GlobalExceptionHandler;
import com.tikitaka.backend.global.exception.CustomException;
import com.tikitaka.backend.global.exception.ErrorCode;
import com.tikitaka.backend.slide.entity.Slide;
import com.tikitaka.backend.slide.repository.SlideRepository;
import com.tikitaka.backend.stroke.dto.DeleteStrokeResponse;
import com.tikitaka.backend.stroke.dto.GetPrivateStrokesResponse;
import com.tikitaka.backend.stroke.dto.GetSharedStrokesResponse;
import com.tikitaka.backend.stroke.dto.SaveStrokesRequest;
import com.tikitaka.backend.stroke.dto.SaveStrokesResponse;
import com.tikitaka.backend.stroke.dto.SharedStrokeMessage;
import com.tikitaka.backend.stroke.service.StrokeService;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Stroke", description = "필기 관련 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_SCHEME_NAME)
public class StrokeController {

    private final StrokeService strokeService;
    private final JwtProvider jwtProvider;
    private final SimpMessagingTemplate messagingTemplate;
    private final SlideRepository slideRepository;

    @PostMapping("/api/v1/slides/{slide_id}/private-strokes")
    @Operation(summary = "개인 필기 작성 및 저장", description = "슬라이드에 개인 필기를 배치 저장합니다. 레이어가 없으면 자동 생성됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "저장 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaveStrokesResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "슬라이드를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<SaveStrokesResponse> savePrivateStrokes(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "슬라이드 ID") @PathVariable("slide_id") UUID slideId,
            @RequestBody @Valid SaveStrokesRequest request
    ) {
        UUID userId = extractUserId(authHeader);

        SaveStrokesResponse response =
                strokeService.savePrivateStrokes(slideId, userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/slides/{slide_id}/private-strokes")
    @Operation(summary = "개인 필기 조회", description = "슬라이드에 저장된 본인의 개인 필기를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetPrivateStrokesResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "슬라이드를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<GetPrivateStrokesResponse> getPrivateStrokes(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "슬라이드 ID") @PathVariable("slide_id") UUID slideId
    ) {
        UUID userId = extractUserId(authHeader);

        GetPrivateStrokesResponse response =
                strokeService.getPrivateStrokes(slideId, userId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/v1/private-strokes/{stroke_id}")
    @Operation(summary = "개인 필기 삭제 처리", description = "개인 필기를 soft delete 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeleteStrokeResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "본인 필기가 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "필기를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<DeleteStrokeResponse> deletePrivateStroke(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "스트로크 ID") @PathVariable("stroke_id") UUID strokeId
    ) {
        UUID userId = extractUserId(authHeader);

        DeleteStrokeResponse response =
                strokeService.deletePrivateStroke(strokeId, userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/slides/{slide_id}/shared-strokes")
    @Operation(summary = "교수 공유 필기 작성 및 저장", description = "교수가 슬라이드에 공유 필기를 저장합니다. 교수 권한만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "저장 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaveStrokesResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "교수 권한 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "슬라이드를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<SaveStrokesResponse> saveSharedStrokes(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "슬라이드 ID") @PathVariable("slide_id") UUID slideId,
            @RequestBody @Valid SaveStrokesRequest request
    ) {
        UUID userId = extractUserId(authHeader);

        SaveStrokesResponse response =
                strokeService.saveSharedStrokes(slideId, userId, request);

        publishSharedStrokeChanged(slideId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/slides/{slide_id}/shared-strokes")
    @Operation(summary = "교수 공유 필기 조회", description = "슬라이드의 교수 공유 필기를 조회합니다. 강의 참여자 전체 열람 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetSharedStrokesResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "슬라이드를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<GetSharedStrokesResponse> getSharedStrokes(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "슬라이드 ID") @PathVariable("slide_id") UUID slideId
    ) {
        UUID userId = extractUserId(authHeader);

        GetSharedStrokesResponse response =
                strokeService.getSharedStrokes(slideId, userId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/v1/shared-strokes/{stroke_id}")
    @Operation(summary = "교수 공유 필기 삭제 처리", description = "교수 공유 필기를 soft delete 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeleteStrokeResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "교수 권한 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "필기를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<DeleteStrokeResponse> deleteSharedStroke(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "스트로크 ID") @PathVariable("stroke_id") UUID strokeId
    ) {
        UUID userId = extractUserId(authHeader);

        DeleteStrokeResponse response =
                strokeService.deleteSharedStroke(strokeId, userId);

        return ResponseEntity.ok(response);
    }

    private void publishSharedStrokeChanged(UUID slideId, SaveStrokesRequest request) {
        Slide slide = slideRepository.findById(slideId)
                .orElseThrow(() -> new CustomException(ErrorCode.SLIDE_NOT_FOUND));

        UUID spaceId = slide.getDocument().getSpace().getId();

        SharedStrokeMessage.StrokeData strokeData = null;

        if (request.getStrokes() != null && !request.getStrokes().isEmpty()) {
            var firstStroke = request.getStrokes().get(0);

            strokeData = SharedStrokeMessage.StrokeData.builder()
                    .tool(firstStroke.getTool().name())
                    .points(firstStroke.getPoints())
                    .color(firstStroke.getColor() != null ? firstStroke.getColor() : "#000000")
                    .thickness(firstStroke.getThickness() != null ? firstStroke.getThickness() : 2.0f)
                    .content(firstStroke.getContent())
                    .strokeOrder(firstStroke.getStrokeOrder() != null ? firstStroke.getStrokeOrder() : 0)
                    .build();
        }

        SharedStrokeMessage broadcast = SharedStrokeMessage.builder()
                .type("SHARED_STROKE_CHANGED")
                .slideId(slideId)
                .stroke(strokeData)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/spaces/" + spaceId + "/slides/" + slideId + "/shared-strokes",
                broadcast
        );
    }

    private UUID extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        String token = authHeader.substring(7);

        jwtProvider.isTokenValid(token);

        return UUID.fromString(jwtProvider.extractUserId(token));
    }
}