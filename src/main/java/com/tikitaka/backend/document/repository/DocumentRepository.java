package com.tikitaka.backend.document.repository;

import com.tikitaka.backend.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findBySpaceIdOrderByCreatedAtDesc(UUID spaceId);
}