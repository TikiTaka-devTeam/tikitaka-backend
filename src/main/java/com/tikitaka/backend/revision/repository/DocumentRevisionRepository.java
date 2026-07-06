package com.tikitaka.backend.revision.repository;

import com.tikitaka.backend.revision.entity.DocumentRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentRevisionRepository extends JpaRepository<DocumentRevision, UUID> {
}
