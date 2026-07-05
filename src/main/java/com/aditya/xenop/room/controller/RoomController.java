package com.aditya.xenop.room.controller;

import com.aditya.xenop.room.dto.request.CreateRoomRequest;
import com.aditya.xenop.room.dto.request.UpdateRoomRequest;
import com.aditya.xenop.room.dto.response.RoomResponse;
import com.aditya.xenop.room.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @Valid @RequestBody CreateRoomRequest request,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        return ResponseEntity.ok(roomService.createRoom(request, userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable UUID id){
        return ResponseEntity.ok(roomService.getRoom(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoomRequest request,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        return ResponseEntity.ok(roomService.updateRoom(id , request , userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        roomService.deleteRoom(id , userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<RoomResponse> joinRoom(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        return ResponseEntity.ok(roomService.joinRoom(id, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        roomService.leaveRoom(id , userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<RoomResponse> startBattle(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        return ResponseEntity.ok(roomService.startBattle(id, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> listRoom(
            @RequestParam(required = false) String status
    ){
        return ResponseEntity.ok(roomService.listRoom(status));
    }
}
