package com.tikitaka.backend.space.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tikitaka.backend.global.jwt.JwtProvider;
import com.tikitaka.backend.space.dto.CreateSpaceRequest;
import com.tikitaka.backend.space.dto.CreateSpaceResponse;
import com.tikitaka.backend.space.service.SpaceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;
    private final JwtProvider jwtProvider;

    @PostMapping
    public ResponseEntity<CreateSpaceResponse> createSpace(
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
