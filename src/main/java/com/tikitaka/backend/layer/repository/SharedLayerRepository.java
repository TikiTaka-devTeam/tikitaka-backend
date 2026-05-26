package com.tikitaka.backend.layer.repository;

import com.tikitaka.backend.layer.entity.SharedLayer;
import com.tikitaka.backend.slide.entity.Slide;
import com.tikitaka.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SharedLayerRepository extends JpaRepository<SharedLayer, UUID> {
    Optional<SharedLayer> findBySlideAndUser(Slide slide, User user);
}
