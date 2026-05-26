package com.tikitaka.backend.stroke.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikitaka.backend.global.exception.CustomException;
import com.tikitaka.backend.global.exception.ErrorCode;
import com.tikitaka.backend.layer.entity.PrivateLayer;
import com.tikitaka.backend.layer.entity.SharedLayer;
import com.tikitaka.backend.layer.repository.PrivateLayerRepository;
import com.tikitaka.backend.layer.repository.SharedLayerRepository;
import com.tikitaka.backend.slide.entity.Slide;
import com.tikitaka.backend.slide.repository.SlideRepository;
import com.tikitaka.backend.stroke.dto.*;
import com.tikitaka.backend.stroke.entity.PrivateStroke;
import com.tikitaka.backend.stroke.entity.SharedStroke;
import com.tikitaka.backend.stroke.entity.StrokeTool;
import com.tikitaka.backend.stroke.repository.PrivateStrokeRepository;
import com.tikitaka.backend.stroke.repository.SharedStrokeRepository;
import com.tikitaka.backend.user.entity.Role;
import com.tikitaka.backend.user.entity.User;
import com.tikitaka.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StrokeService {

    private final SlideRepository slideRepository;
    private final UserRepository userRepository;
    private final PrivateLayerRepository privateLayerRepository;
    private final SharedLayerRepository sharedLayerRepository;
    private final PrivateStrokeRepository privateStrokeRepository;
    private final SharedStrokeRepository sharedStrokeRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public SaveStrokesResponse savePrivateStrokes(UUID slideId, UUID userId, SaveStrokesRequest request) {
        Slide slide = findSlide(slideId);
        User user = findUser(userId);

        PrivateLayer layer = privateLayerRepository.findBySlideAndUser(slide, user)
                .orElseGet(() -> privateLayerRepository.save(
                        PrivateLayer.builder().slide(slide).user(user).build()
                ));

        List<PrivateStroke> saved = request.getStrokes().stream()
                .map(req -> privateStrokeRepository.save(
                        PrivateStroke.builder()
                                .layer(layer)
                                .tool(req.getTool().name())
                                .points(toJson(req.getPoints()))
                                .content(req.getContent())
                                .color(req.getColor() != null ? req.getColor() : "#000000")
                                .thickness(req.getThickness() != null ? req.getThickness() : 2.0f)
                                .strokeOrder(req.getStrokeOrder() != null ? req.getStrokeOrder() : 0)
                                .build()
                )).toList();

        return new SaveStrokesResponse(slideId, saved.size(), saved.stream().map(PrivateStroke::getId).toList());
    }

    @Transactional(readOnly = true)
    public GetPrivateStrokesResponse getPrivateStrokes(UUID slideId, UUID userId) {
        Slide slide = findSlide(slideId);
        User user = findUser(userId);

        List<StrokeResponse> strokes = privateLayerRepository.findBySlideAndUser(slide, user)
                .map(layer -> privateStrokeRepository.findByLayerAndIsDeletedFalseOrderByStrokeOrderAsc(layer)
                        .orElse(Collections.emptyList()))
                .orElse(Collections.emptyList())
                .stream()
                .map(StrokeResponse::from)
                .toList();

        return new GetPrivateStrokesResponse(slideId, slide.getImageUrl(), strokes);
    }

    @Transactional
    public DeleteStrokeResponse deletePrivateStroke(UUID strokeId, UUID userId) {
        PrivateStroke stroke = privateStrokeRepository.findById(strokeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRIVATE_STROKE_NOT_FOUND));

        if (!stroke.getLayer().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.STROKE_ACCESS_DENIED);
        }

        stroke.markDeleted();
        return new DeleteStrokeResponse(strokeId, true);
    }

    @Transactional
    public SaveStrokesResponse saveSharedStrokes(UUID slideId, UUID userId, SaveStrokesRequest request) {
        Slide slide = findSlide(slideId);
        User user = findUser(userId);

        if (user.getRole() != Role.PROFESSOR) {
            throw new CustomException(ErrorCode.PROFESSOR_ONLY);
        }

        SharedLayer layer = sharedLayerRepository.findBySlideAndUser(slide, user)
                .orElseGet(() -> sharedLayerRepository.save(
                        SharedLayer.builder().slide(slide).user(user).build()
                ));

        List<SharedStroke> saved = request.getStrokes().stream()
                .map(req -> {
                    if (req.getTool() == StrokeTool.Q_POINT) {
                        throw new CustomException(ErrorCode.Q_POINT_NOT_ALLOWED);
                    }
                    return sharedStrokeRepository.save(
                        SharedStroke.builder()
                                .layer(layer)
                                .tool(req.getTool().name())
                                .points(toJson(req.getPoints()))
                                .content(req.getContent())
                                .color(req.getColor() != null ? req.getColor() : "#000000")
                                .thickness(req.getThickness() != null ? req.getThickness() : 2.0f)
                                .strokeOrder(req.getStrokeOrder() != null ? req.getStrokeOrder() : 0)
                                .build()
                    );
                }).toList();

        return new SaveStrokesResponse(slideId, saved.size(), saved.stream().map(SharedStroke::getId).toList());
    }

    @Transactional(readOnly = true)
    public GetSharedStrokesResponse getSharedStrokes(UUID slideId, UUID userId) {
        Slide slide = findSlide(slideId);
        User professor = slide.getDocument().getSpace().getProfessor();

        List<StrokeResponse> strokes = sharedLayerRepository.findBySlideAndUser(slide, professor)
                .map(layer -> sharedStrokeRepository.findByLayerAndIsDeletedFalseOrderByStrokeOrderAsc(layer)
                        .orElse(Collections.emptyList()))
                .orElse(Collections.emptyList())
                .stream()
                .map(StrokeResponse::from)
                .toList();

        return new GetSharedStrokesResponse(slideId, true, strokes);
    }

    @Transactional
    public DeleteStrokeResponse deleteSharedStroke(UUID strokeId, UUID userId) {
        SharedStroke stroke = sharedStrokeRepository.findById(strokeId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_STROKE_NOT_FOUND));

        if (!stroke.getLayer().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.STROKE_ACCESS_DENIED);
        }

        stroke.markDeleted();
        return new DeleteStrokeResponse(strokeId, true);
    }

    private Slide findSlide(UUID slideId) {
        return slideRepository.findById(slideId)
                .orElseThrow(() -> new CustomException(ErrorCode.SLIDE_NOT_FOUND));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
    }
}