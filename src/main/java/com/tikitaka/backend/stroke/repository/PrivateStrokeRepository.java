package com.tikitaka.backend.stroke.repository;

import com.tikitaka.backend.stroke.entity.PrivateStroke;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrivateStrokeRepository extends JpaRepository<PrivateStroke, UUID> {
}