package com.tikitaka.backend.stroke.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class SaveStrokesResponse {
    private UUID slideId;
    private int savedCount;
    private List<UUID> strokeIds;
}
