package com.tikitaka.backend.space.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tikitaka.backend.space.dto.SpaceSummaryResponse;
import com.tikitaka.backend.space.entity.SpaceMember;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
public interface SpaceMemberRepository extends JpaRepository<SpaceMember, UUID> {
    @Query("""
        select new com.tikitaka.backend.space.dto.SpaceSummaryResponse(
            sm.space.id,
            sm.space.name,
            sm.nickname,
            sm.space.semester,
            sm.space.color,
            sm.space.professor.name
        )
        from SpaceMember sm
        where sm.user.id = :userId
          and sm.validity = 'APPROVED'
        order by sm.requestedAt desc
        """)
    List<SpaceSummaryResponse> findApprovedSpacesByUserId(@Param("userId") UUID userId);
}
