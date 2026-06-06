package com.tikitaka.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProfileImagePresignedUrlResponse(
    @JsonProperty("upload_url")
    String uploadUrl,

    @JsonProperty("object_key")
    String objectKey,

    @JsonProperty("profile_url")
    String profileUrl,

    @JsonProperty("expires_in_seconds")
    long expiresInSeconds
) {
}
