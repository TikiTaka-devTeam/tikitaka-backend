package com.tikitaka.backend.stroke.util;

import com.tikitaka.backend.stroke.dto.StrokePoint;
import java.util.ArrayList;
import java.util.List;

public class StrokePointSampler {

    private static final double MIN_DISTANCE = 1.0;
    private static final int MAX_POINTS = 500;

    public static List<StrokePoint> sample(List<StrokePoint> points) {
        if (points == null || points.isEmpty()) return List.of();
        if (points.size() == 1) return List.copyOf(points);

        List<StrokePoint> sampled = new ArrayList<>();
        sampled.add(points.get(0));

        for (int i = 1; i < points.size() - 1; i++) {
            StrokePoint last = sampled.get(sampled.size() - 1);
            StrokePoint curr = points.get(i);
            if (distance(last, curr) >= MIN_DISTANCE) {
                sampled.add(curr);
            }
        }

        // 마지막 점은 항상 포함
        StrokePoint lastPoint = points.get(points.size() - 1);
        if (sampled.get(sampled.size() - 1) != lastPoint) {
            sampled.add(lastPoint);
        }

        if (sampled.size() <= MAX_POINTS) return sampled;

        // 최대 포인트 초과 시 균등 샘플링
        List<StrokePoint> limited = new ArrayList<>(MAX_POINTS);
        limited.add(sampled.get(0));
        double step = (double)(sampled.size() - 2) / (MAX_POINTS - 2);
        for (int i = 1; i < MAX_POINTS - 1; i++) {
            limited.add(sampled.get((int)(i * step)));
        }
        limited.add(sampled.get(sampled.size() - 1));
        return limited;
    }

    private static double distance(StrokePoint a, StrokePoint b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
