package com.tikitaka.backend.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tikitaka.backend.global.config.SwaggerConfig;
import com.tikitaka.backend.global.config.security.CurrentUserProvider;
import com.tikitaka.backend.global.storage.S3StorageService;
import com.tikitaka.backend.user.dto.UserProfileResponse;
import com.tikitaka.backend.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 프로필 API")
@SecurityRequirement(name = SwaggerConfig.BEARER_SCHEME_NAME)
public class UserController {

    private final CurrentUserProvider currentUserProvider;
    private final S3StorageService s3StorageService;

    @GetMapping
    @Operation(
        summary = "내 프로필 조회",
        description = "현재 로그인한 사용자의 프로필 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "내 프로필 조회 성공",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        User currentUser = currentUserProvider.getCurrentUser();
        String profileUrl = s3StorageService.toCloudFrontUrl(currentUser.getProfileUrl());
        return ResponseEntity.ok(UserProfileResponse.from(currentUser, profileUrl));
    }
}
