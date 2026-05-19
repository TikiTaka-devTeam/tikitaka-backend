package com.tikitaka.backend.space.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateSpaceResponse(
    @JsonProperty("space_id")
    UUID spaceId,

    String name,

    String nickname,

    String semester,

    String color,

    @JsonProperty("space_code")
    String spaceCode
) {}
