package com.tikitaka.backend.dashboard.repository;

import com.tikitaka.backend.space.member.SpaceMember;
import com.tikitaka.backend.space.schedule.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface DashboardRepository extends JpaRepository<SpaceMember, UUID> {

    // 최근 접속한 강의 2개 조회
    @Query("""
        SELECT sm FROM SpaceMember sm
        JOIN FETCH sm.space s
        JOIN FETCH s.professor p
        WHERE sm.user.id = :userId
        AND sm.validity = 'APPROVED'
        AND sm.lastAccessedAt IS NOT NULL
        ORDER BY sm.lastAccessedAt DESC
        LIMIT 2
    """)
    List<SpaceMember> findRecentSpaces(@Param("userId") UUID userId);

    // 시간표 조회
    @Query("""
        SELECT sc FROM Schedule sc
        JOIN FETCH sc.space s
        JOIN sm ON sm.space.id = s.id
        WHERE sm.user.id = :userId
        AND sm.validity = 'APPROVED'
        ORDER BY sc.day, sc.startTime
    """)
    List<Schedule> findSchedulesByUserId(@Param("userId") UUID userId);

    // 다음 강의 조회
    @Query("""
        SELECT sc FROM Schedule sc
        JOIN FETCH sc.space s
        JOIN FETCH s.professor p
        JOIN SpaceMember sm ON sm.space.id = s.id
        WHERE sm.user.id = :userId
        AND sm.validity = 'APPROVED'
        AND sc.day = :day
        AND sc.startTime > :currentTime
        ORDER BY sc.startTime ASC
        LIMIT 1
    """)
    List<Schedule> findNextSchedule(
        @Param("userId") UUID userId,
        @Param("day") String day,
        @Param("currentTime") LocalTime currentTime
    );
}