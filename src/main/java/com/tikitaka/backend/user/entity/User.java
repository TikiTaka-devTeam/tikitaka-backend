package com.tikitaka.backend.user.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor // 파라미터가 없는 기본 생성자 생성 -> JPA에서 엔티티 클래스를 인스턴스화할 때 필요
public class User {
    @Id
    @UuidGenerator // UUID를 자동으로 생성하는 어노테이션
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = true) // 소셜 로그인 시 비밀번호는 null 가능
    private String password;

    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = true) // 소셜 로그인 시 비밀번호는 null 가능
    private String phoneNumber;

    @Column(nullable = false)
    private String univ;

    @Column(nullable = false)
    private String major;

    @Column(name = "member_id_number") // 학번, null 가능
    private String memberIdNumber;

    @Column(name = "profile_url") // 프로필 이미지 경로, null 가능
    private String profileUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
