package com.tikitaka.backend.space.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tikitaka.backend.space.dto.SpaceMemberSummaryResponse;
import com.tikitaka.backend.space.dto.SpaceSummaryResponse;
import com.tikitaka.backend.space.entity.SpaceMember;
import com.tikitaka.backend.user.entity.User;


import io.swagger.v3.oas.annotations.Hidden;

@Hidden
public interface SpaceMemberRepository extends JpaRepository<SpaceMember, UUID> {
    @Query("""
        select new com.tikitaka.backend.space.dto.SpaceSummaryResponse(
            sm.space.id,
            sm.space.name,
            sm.nickname,
            sm.space.semester,
            coalesce(sm.color, sm.space.color),
            sm.space.professor.name
        )
        from SpaceMember sm
        where sm.user.id = :userId
          and sm.validity = 'APPROVED'
        order by sm.requestedAt desc
        """)
    List<SpaceSummaryResponse> findApprovedSpacesByUserId(@Param("userId") UUID userId);
    Optional<SpaceMember> findBySpaceIdAndUserId(UUID spaceId, UUID userId);

    @Query("""
        select new com.tikitaka.backend.space.dto.SpaceMemberSummaryResponse(
            sm.id,
            sm.user.id,
            sm.user.name,
            sm.user.memberIdNumber
        )
        from SpaceMember sm
        where sm.space.id = :spaceId
          and sm.validity = :validity
        order by sm.requestedAt asc
        """)
    List<SpaceMemberSummaryResponse> findMembersBySpaceIdAndValidity(
        @Param("spaceId") UUID spaceId,
        @Param("validity") String validity
    );

    Optional<SpaceMember> findByIdAndSpaceId(UUID id, UUID spaceId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update SpaceMember sm
        set sm.nickname = coalesce(:nickname, sm.nickname),
            sm.color = coalesce(:color, sm.color)
        where sm.id = :memberId
        """)
    int updateNicknameAndColor(
        @Param("memberId") UUID memberId,
        @Param("nickname") String nickname,
        @Param("color") String color
    );
    @Query("""
    SELECT COUNT(sm) > 0
    FROM SpaceMember sm
    WHERE sm.space.id = :spaceId
      AND sm.user.id = :userId
      AND sm.validity = :validity
    """)
    boolean existsBySpaceIdAndUserIdAndValidity(
            @Param("spaceId") UUID spaceId,
            @Param("userId") UUID userId,
            @Param("validity") String validity
    );

    @Query("""
    select sm.user
    from SpaceMember sm
    where sm.space.id = :spaceId
      and sm.validity = 'APPROVED'
      and sm.user.role = com.tikitaka.backend.user.entity.Role.STUDENT
    """)
    List<User> findApprovedStudentsBySpaceId(@Param("spaceId") UUID spaceId);
}
