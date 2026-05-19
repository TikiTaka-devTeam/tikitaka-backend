package com.tikitaka.backend.space.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tikitaka.backend.space.entity.SpaceMember;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
public interface SpaceMemberRepository extends JpaRepository<SpaceMember, UUID> {
}
