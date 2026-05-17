package com.tikitaka.backend.auth.dto;

import com.tikitaka.backend.user.entity.Role;

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;

@Getter
@NoArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "대학교는 필수입니다")
    private String univ;

    @NotBlank(message = "학과는 필수입니다")
    private String major;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "역할은 필수입니다")
    private Role role;          // STUDENT / PROFESSOR

    private String phoneNumber; // 선택
    private String memberIdNumber; // 선택
}
