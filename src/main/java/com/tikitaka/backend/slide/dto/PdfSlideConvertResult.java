package com.tikitaka.backend.slide.dto;

import java.util.List;

public record PdfSlideConvertResult(
        List<String> slideImageUrls,
        int pageCount,
        String thumbnailUrl
) {
}