package com.tikitaka.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProfileImageResponse(
    @JsonProperty("profile_url")
    String profileUrl
) {
}
