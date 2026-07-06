package com.tikitaka.backend.stroke.repository;

import com.tikitaka.backend.stroke.entity.Fixer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FixerRepository extends JpaRepository<Fixer, UUID> {

    List<Fixer> findByLayerSlideIdAndProfessorIdOrderByCreatedAtDesc(
            UUID slideId,
            UUID professorId
    );

    Optional<Fixer> findByIdAndProfessorId(
            UUID fixerId,
            UUID professorId
    );
}