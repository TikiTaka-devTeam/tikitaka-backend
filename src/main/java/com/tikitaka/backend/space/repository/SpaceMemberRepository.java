package com.tikitaka.backend.space.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tikitaka.backend.space.entity.SpaceMember;

public interface SpaceMemberRepository extends JpaRepository<SpaceMember, UUID> {
}
