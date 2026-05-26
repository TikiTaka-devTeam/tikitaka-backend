package com.tikitaka.backend.stroke.repository;

import com.tikitaka.backend.layer.entity.SharedLayer;
import com.tikitaka.backend.stroke.entity.SharedStroke;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SharedStrokeRepository extends JpaRepository<SharedStroke, UUID> {
    Optional<List<SharedStroke>> findByLayerAndIsDeletedFalseOrderByStrokeOrderAsc(SharedLayer layer);
}