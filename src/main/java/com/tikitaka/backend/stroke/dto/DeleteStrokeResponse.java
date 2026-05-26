package com.tikitaka.backend.stroke.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class DeleteStrokeResponse {
    private UUID strokeId;
    private Boolean isDeleted;
}
