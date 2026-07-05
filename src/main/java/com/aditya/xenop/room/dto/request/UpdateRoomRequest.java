package com.aditya.xenop.room.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;
@Data
public class UpdateRoomRequest {
    @Size(max = 100)
    private String title;
    private String description;
    private String battleType;

    @Min(value = 2 , message = "Minimum 2 Participants")
    @Max(value = 16, message = "Maximum 16 Participants")
    private Integer maxParticipants;
    private Map<String, Object> battleConfig;
}
