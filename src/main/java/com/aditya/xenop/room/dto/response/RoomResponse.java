package com.aditya.xenop.room.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class RoomResponse {
    private UUID id;
    private String title;
    private String description;
    private String battleType;
    private String status;
    private String creatorUsername;
    private Integer maxParticipants;
    private Long currentParticipants;
    private Map<String, Object> battleConfig;
    private Instant createdAt;
}
