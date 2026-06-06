package com.tikitaka.backend.stroke.repository;

import com.tikitaka.backend.layer.entity.SharedLayer;
import com.tikitaka.backend.stroke.entity.SharedStroke;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SharedStrokeRepository extends JpaRepository<SharedStroke, UUID> {
    Optional<List<SharedStroke>> findByLayerAndIsDeletedFalseOrderByStrokeOrderAsc(SharedLayer layer);

    @Query("SELECT MAX(s.strokeSeq) FROM SharedStroke s WHERE s.layer = :layer")
    Optional<Integer> findMaxStrokeSeqByLayer(@Param("layer") SharedLayer layer);

    @Query("SELECT s FROM SharedStroke s WHERE s.layer.slide.id = :slideId AND s.strokeSeq > :lastSeq AND s.isDeleted = false ORDER BY s.strokeSeq ASC")
    List<SharedStroke> findMissingStrokesBySlideId(@Param("slideId") UUID slideId, @Param("lastSeq") int lastSeq);
}