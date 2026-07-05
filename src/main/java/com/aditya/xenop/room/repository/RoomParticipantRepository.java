package com.aditya.xenop.room.repository;

import com.aditya.xenop.room.entity.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, UUID> {
    List<RoomParticipant> findByRoomId(UUID roomId);
    
    Optional<RoomParticipant> findByRoomIdAndUserId(UUID roomId, UUID userId);

    boolean existsByRoomIdAndUserId(UUID roomId, UUID userId);

    @Query("SELECT COUNT(rp) > 0 FROM RoomParticipant rp WHERE rp.user.id = :userId AND rp.room.status != com.aditya.xenop.room.enums.Status.CLOSED")
    boolean existsByUserIdAndRoomStatusNotClosed(@Param("userId") UUID userId);

    long countByRoomId(UUID roomId);
}


