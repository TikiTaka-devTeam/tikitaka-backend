package com.tikitaka.backend.revision.repository;

import com.tikitaka.backend.revision.entity.RevisionSlide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RevisionSlideRepository extends JpaRepository<RevisionSlide, UUID> {

    boolean existsByTargetSlideIdAndIdNot(UUID targetSlideId, UUID id);
}
