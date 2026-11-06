package com.example.hotel.web;

import com.example.hotel.dto.ApiDtos.*;
import com.example.hotel.service.HotelService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class PublicRoomController {
    private final HotelService hotel;
    public PublicRoomController(HotelService hotel) { this.hotel = hotel; }

    @GetMapping("/api/rooms")
    public Page<RoomResponse> rooms(@RequestParam(required = false) String search,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return hotel.publicRooms(search, page, size);
    }

    @GetMapping("/api/rooms/{id}")
    public RoomResponse room(@PathVariable Long id) { return hotel.publicRoom(id); }

    @GetMapping("/api/room-types")
    public List<RoomTypeResponse> roomTypes() { return hotel.roomTypes(); }
}
