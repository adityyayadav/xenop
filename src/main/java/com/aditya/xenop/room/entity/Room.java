package com.aditya.xenop.room.entity;

import com.aditya.xenop.auth.entity.User;
import com.aditya.xenop.room.enums.Status;
import com.aditya.xenop.room.enums.Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "battle_type" , nullable = false)
    @Builder.Default
    private Type battleType = Type.MUSIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.WAITING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id" , nullable = false)
    private User creator;

    @Column(name = "max_participants" , nullable = false)
    @Builder.Default
    private Integer maxParticipants = 8;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "battle_config" , columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> battleConfig = new HashMap<>();

    @Version
    private Long version;
    @Column(name = "created_at" , updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = Instant.now();
    }
}
