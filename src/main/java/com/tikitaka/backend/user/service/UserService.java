package com.tikitaka.backend.user.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tikitaka.backend.global.exception.CustomException;
import com.tikitaka.backend.global.exception.ErrorCode;
import com.tikitaka.backend.user.dto.UserProfileUpdateRequest;
import com.tikitaka.backend.user.entity.User;
import com.tikitaka.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public User updateMyProfile(UUID userId, UserProfileUpdateRequest request) {
        validateNotBlankIfPresent(request.name(), "이름은 비어 있을 수 없습니다.");
        validateNotBlankIfPresent(request.phoneNumber(), "전화번호는 비어 있을 수 없습니다.");
        validateNotBlankIfPresent(request.major(), "전공은 비어 있을 수 없습니다.");

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.updateProfile(
            request.name(),
            request.phoneNumber(),
            request.major()
        );

        return user;
    }

    private void validateNotBlankIfPresent(String value, String message) {
        if (value != null && value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
