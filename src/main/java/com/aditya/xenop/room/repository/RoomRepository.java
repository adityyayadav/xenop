package com.aditya.xenop.room.repository;

import com.aditya.xenop.room.entity.Room;
import com.aditya.xenop.room.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findByStatus(Status status);

    List<Room> findByCreatorId(UUID creatorId);

}
