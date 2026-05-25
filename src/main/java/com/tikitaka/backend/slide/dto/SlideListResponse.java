package com.tikitaka.backend.slide.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.slide.entity.Slide;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class SlideListResponse {

    @JsonProperty("slide_id")
    private UUID slideId;

    @JsonProperty("page_number")
    private Integer pageNumber;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("is_replaced")
    private Boolean isReplaced;

    @JsonProperty("is_deleted")
    private Boolean isDeleted;

    @JsonProperty("original_slide_id")
    private UUID originalSlideId;

    private String watermark;

    public static SlideListResponse from(Slide slide) {
        return SlideListResponse.builder()
                .slideId(slide.getId())
                .pageNumber(slide.getPageNumber())
                .imageUrl(slide.getImageUrl())
                .isReplaced(slide.getIsReplaced())
                .isDeleted(slide.getIsDeleted())
                .originalSlideId(
                        slide.getOriginalSlide() == null
                                ? null
                                : slide.getOriginalSlide().getId()
                )
                .watermark(getWatermark(slide))
                .build();
    }

    private static String getWatermark(Slide slide) {
        if (Boolean.TRUE.equals(slide.getIsDeleted())) {
            return "삭제된 강의자료입니다.";
        }

        if (Boolean.TRUE.equals(slide.getIsReplaced())) {
            return "수정된 강의자료입니다.";
        }

        return null;
    }
}