package com.tikitaka.backend.question.repository;

import com.tikitaka.backend.question.entity.Question;
import com.tikitaka.backend.question.entity.QuestionLike;
import com.tikitaka.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuestionLikeRepository extends JpaRepository<QuestionLike, UUID> {

    boolean existsByQuestionAndUser(Question question, User user);

    Optional<QuestionLike> findByQuestionAndUser(Question question, User user);
}