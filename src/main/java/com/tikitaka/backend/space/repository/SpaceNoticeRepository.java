package com.tikitaka.backend.space.repository;

import com.tikitaka.backend.space.entity.SpaceNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpaceNoticeRepository extends JpaRepository<SpaceNotice, UUID> {

    List<SpaceNotice> findBySpaceIdOrderByIsPinnedDescCreatedAtDesc(UUID spaceId);
}