package com.tikitaka.backend.stroke.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.stroke.entity.Fixer;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class FixerCheckResponse {

    @JsonProperty("fixer_id")
    private UUID fixerId;

    @JsonProperty("is_checked")
    private Boolean isChecked;

    public static FixerCheckResponse from(Fixer fixer) {
        return FixerCheckResponse.builder()
                .fixerId(fixer.getId())
                .isChecked(fixer.getIsChecked())
                .build();
    }
}