package com.tikitaka.backend.token.controller;

import com.tikitaka.backend.global.config.SwaggerConfig;
import com.tikitaka.backend.global.config.security.CurrentUserProvider;
import com.tikitaka.backend.token.dto.DeviceTokenRegisterRequest;
import com.tikitaka.backend.token.dto.DeviceTokenResponse;
import com.tikitaka.backend.token.dto.PushTestRequest;
import com.tikitaka.backend.token.dto.PushTestResponse;
import com.tikitaka.backend.token.service.DeviceTokenService;
import com.tikitaka.backend.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Device Token", description = "기기 푸시 토큰 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_SCHEME_NAME)
public class DeviceTokenController {

    private final CurrentUserProvider currentUserProvider;
    private final DeviceTokenService deviceTokenService;

    @PostMapping("/device-tokens")
    @Operation(
        summary = "사용자 기기의 푸시 토큰 등록",
        description = "현재 로그인한 사용자의 기기 FCM 토큰을 등록합니다. device_type = [ANDROID | IOS | WEB]"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "기기 토큰 등록 성공",
            content = @Content(schema = @Schema(implementation = DeviceTokenResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 입력값"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰")
    })
    public ResponseEntity<DeviceTokenResponse> registerDeviceToken(
        @RequestBody @Valid DeviceTokenRegisterRequest request
    ) {
        User currentUser = currentUserProvider.getCurrentUser();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(deviceTokenService.register(currentUser, request));
    }

    @DeleteMapping("/device-tokens/{device_token_id}")
    @Operation(
        summary = "사용자 기기의 푸시 토큰 비활성화",
        description = "현재 로그인한 사용자의 기기 토큰을 비활성화합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "기기 토큰 비활성화 성공",
            content = @Content(schema = @Schema(implementation = DeviceTokenResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
        @ApiResponse(responseCode = "404", description = "기기 토큰을 찾을 수 없음")
    })
    public ResponseEntity<DeviceTokenResponse> deactivateDeviceToken(
        @PathVariable("device_token_id") UUID deviceTokenId
    ) {
        User currentUser = currentUserProvider.getCurrentUser();

        return ResponseEntity.ok(deviceTokenService.deactivate(currentUser.getId(), deviceTokenId));
    }

    @PostMapping("/push/test")
    @Operation(
        summary = "현재 사용자 기기로 푸시 테스트 발송",
        description = "현재 로그인한 사용자의 활성 기기 토큰으로 테스트 푸시 발송을 요청합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "푸시 테스트 발송 성공",
            content = @Content(schema = @Schema(implementation = PushTestResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 입력값"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
        @ApiResponse(responseCode = "404", description = "활성화된 기기 토큰을 찾을 수 없음")
    })
    public ResponseEntity<PushTestResponse> sendTestPush(
        @RequestBody @Valid PushTestRequest request
    ) {
        User currentUser = currentUserProvider.getCurrentUser();

        return ResponseEntity.ok(deviceTokenService.sendTestPush(currentUser.getId(), request));
    }
}
