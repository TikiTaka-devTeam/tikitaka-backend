package com.tikitaka.backend.space.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tikitaka.backend.space.entity.Space;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
public interface SpaceRepository extends JpaRepository<Space, UUID> {
    boolean existsBySpaceCode(String spaceCode);
    Optional<Space> findBySpaceCode(String spaceCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Space s
        set s.name = coalesce(:name, s.name),
            s.semester = coalesce(:semester, s.semester),
            s.color = coalesce(:color, s.color)
        where s.id = :spaceId
        """)
    int updateSpaceInfo(
        @Param("spaceId") UUID spaceId,
        @Param("name") String name,
        @Param("semester") String semester,
        @Param("color") String color
    );
}
