package com.tikitaka.backend.stroke.repository;

import com.tikitaka.backend.layer.entity.PrivateLayer;
import com.tikitaka.backend.stroke.entity.PrivateStroke;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrivateStrokeRepository extends JpaRepository<PrivateStroke, UUID> {
    Optional<List<PrivateStroke>> findByLayerAndIsDeletedFalseOrderByStrokeOrderAsc(PrivateLayer layer);
}