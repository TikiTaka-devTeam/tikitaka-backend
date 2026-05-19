package com.tikitaka.backend.space.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tikitaka.backend.space.entity.Space;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
public interface SpaceRepository extends JpaRepository<Space, UUID> {
    boolean existsBySpaceCode(String spaceCode);
}
