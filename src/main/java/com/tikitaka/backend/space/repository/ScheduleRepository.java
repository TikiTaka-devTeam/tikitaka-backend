package com.tikitaka.backend.space.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tikitaka.backend.space.entity.Schedule;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    List<Schedule> findBySpaceIdOrderByDayAscStartTimeAsc(UUID spaceId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from Schedule s
        where s.space.id = :spaceId
        """)
    int deleteBySpaceId(@Param("spaceId") UUID spaceId);
}
