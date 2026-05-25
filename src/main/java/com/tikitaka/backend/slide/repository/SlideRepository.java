package com.tikitaka.backend.slide.repository;

import com.tikitaka.backend.slide.entity.Slide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SlideRepository extends JpaRepository<Slide, UUID> {
}