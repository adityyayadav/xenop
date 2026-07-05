package com.aditya.xenop.room.service;

import com.aditya.xenop.auth.entity.User;
import com.aditya.xenop.auth.repository.UserRepository;
import com.aditya.xenop.room.dto.request.CreateRoomRequest;
import com.aditya.xenop.room.dto.request.UpdateRoomRequest;
import com.aditya.xenop.room.dto.response.RoomResponse;
import com.aditya.xenop.room.entity.Room;
import com.aditya.xenop.room.entity.RoomParticipant;
import com.aditya.xenop.room.enums.Status;
import com.aditya.xenop.room.enums.Type;
import com.aditya.xenop.room.repository.RoomParticipantRepository;
import com.aditya.xenop.room.repository.RoomRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final UserRepository userRepository;

    @Transactional
    public RoomResponse createRoom(@Valid CreateRoomRequest request, String email) {
        User creator = findUsersByEmail(email);

        Type battleType = Type.MUSIC;
        if (request.getBattleType() != null) {
            battleType = Type.valueOf(request.getBattleType().toUpperCase());
        }

        if(roomParticipantRepository.existsByUserIdAndRoomStatusNotClosed(creator.getId())){
            throw new IllegalStateException("Already in Room");
        }
        Room room = Room.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .battleType(battleType)
                .maxParticipants(request.getMaxParticipants() != null ? request.getMaxParticipants() : 8)
                .battleConfig(request.getBattleConfig() != null ? request.getBattleConfig() : new HashMap<>())
                .creator(creator)
                .build();

        roomRepository.save(room);

        // creator Auto joins As participant
        RoomParticipant participant = RoomParticipant.builder()
                .room(room)
                .user(creator)
                .build();

        roomParticipantRepository.save(participant);

        return toResponse(room, 1L);
    }

    private RoomResponse toResponse(Room room, long participantCount) {
        return RoomResponse.builder()
                .id(room.getId())
                .title(room.getTitle())
                .description(room.getDescription())
                .battleType(room.getBattleType().name())
                .status(room.getStatus().name())
                .creatorUsername(room.getCreator().getUsername())
                .maxParticipants(room.getMaxParticipants())
                .currentParticipants(participantCount)
                .battleConfig(room.getBattleConfig())
                .createdAt(room.getCreatedAt())
                .build();
    }

    private User findUsersByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not Found"));
    }

    public RoomResponse getRoom(UUID id) {
        Room room = findRoomById(id);
        long count = roomParticipantRepository.countByRoomId(id);
        return toResponse(room,count);
    }

    private Room findRoomById(UUID id) {
        return roomRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Room not found!"));
    }

    @Transactional
    public RoomResponse updateRoom(UUID id, @Valid UpdateRoomRequest request, String email) {

        Room room = findRoomById(id);

        if(!room.getCreator().getEmail().equals(email)){
            throw new IllegalStateException("Only host can perform this action::");
        }

        if(room.getStatus() != Status.WAITING){
            throw new IllegalStateException("Room must be in WAITING state!");
        }

        if(request.getTitle() != null) room.setTitle(request.getTitle());
        if(request.getDescription() != null) room.setDescription(request.getDescription());
        if(request.getMaxParticipants() != null) room.setMaxParticipants(request.getMaxParticipants());
        if(request.getBattleConfig() != null) room.setBattleConfig(request.getBattleConfig());
        if(request.getBattleType() != null) room.setBattleType(Type.valueOf(request.getBattleType().toUpperCase()));

        roomRepository.save(room);
        long count = roomParticipantRepository.countByRoomId(id);
        return toResponse(room, count);
    }

    @Transactional
    public void deleteRoom(UUID id, String email) {
        Room room = findRoomById(id);

        if(!room.getCreator().getEmail().equals(email)){
            throw new IllegalStateException("Only host can perform this operation!!");
        }
        roomRepository.delete(room);
    }

    @Transactional
    public RoomResponse joinRoom(UUID id, String email) {
        Room room = findRoomById(id);
        User user = findUsersByEmail(email);

        if(room.getStatus() != Status.WAITING){
            throw new IllegalStateException("Room must be in Waiting state");
        }

        if(roomParticipantRepository.existsByRoomIdAndUserId(id, user.getId()) ||
                roomParticipantRepository.existsByUserIdAndRoomStatusNotClosed(user.getId())){
            throw new IllegalStateException("Already in the room");
        }

        long count = roomParticipantRepository.countByRoomId(id);
        if(count >= room.getMaxParticipants()){
            throw new IllegalStateException("Room is Full");
        }

        RoomParticipant participant = RoomParticipant.builder()
                .room(room)
                .user(user)
                .build();

        roomParticipantRepository.save(participant);
        return toResponse(room,count + 1);
    }

    @Transactional
    public void leaveRoom(UUID id, String email) {
        Room room = findRoomById(id);
        User user = findUsersByEmail(email);

        if (room.getStatus() != Status.WAITING) {
            throw new IllegalStateException("Room should in Waiting State");
        }

        if (room.getCreator().getId().equals(user.getId())) {
            List<RoomParticipant> participants = roomParticipantRepository.findByRoomId(id);

            RoomParticipant creator = participants.stream()
                    .filter(p -> p.getUser().getId().equals(user.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("You are not in this Room"));

            roomParticipantRepository.delete(creator);

            participants.remove(creator);

            if(participants.isEmpty()){
                roomRepository.delete(room);
            }
            else{
                RoomParticipant newCreator = participants.get(new Random().nextInt(participants.size()));
                room.setCreator(newCreator.getUser());
                roomRepository.save(room);
            }

            return;

        }

        RoomParticipant participant = roomParticipantRepository
                .findByRoomIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalStateException("You are not in this Room!!"));

        roomParticipantRepository.delete(participant);
    }

    @Transactional
    public RoomResponse startBattle(UUID id, String email) {
        Room room = findRoomById(id);
        if(!room.getCreator().getEmail().equals(email)){
            throw new IllegalStateException("Only Host can Start Battle");
        }

        if(room.getStatus() != Status.WAITING){
            throw new IllegalStateException("Room should be in Waiting State!");
        }

        long count = roomParticipantRepository.countByRoomId(id);
        if(count < 2){
            throw new IllegalStateException("Cannot start in Less than 2 Participants");
        }

        room.setStatus(Status.ACTIVE);
        roomRepository.save(room);
        return toResponse(room,count);
    }


    public List<RoomResponse> listRoom(String status) {
        List <Room> rooms;
        if(status != null){
            rooms = roomRepository.findByStatus(Status.valueOf(status.toUpperCase()));
        }
        else{
            rooms = roomRepository.findAll();
        }

        return rooms.stream().map(room ->
        {
            long count = roomParticipantRepository.countByRoomId(room.getId());
            return toResponse(room,count);
        }).toList();
    }
}
