package com.tikitaka.backend.question.repository;

import com.tikitaka.backend.question.answer.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    Optional<Answer> findByQuestionId(UUID questionId);

    boolean existsByQuestionId(UUID questionId);
}