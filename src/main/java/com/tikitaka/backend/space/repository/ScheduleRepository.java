package com.tikitaka.backend.space.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tikitaka.backend.space.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
}
