package com.example.hotel.config;

import com.example.hotel.entity.RoomInformation;
import com.example.hotel.entity.RoomStatus;
import com.example.hotel.entity.RoomType;
import com.example.hotel.repository.RoomInformationRepository;
import com.example.hotel.repository.RoomTypeRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DevelopmentData {
    @Bean
    ApplicationRunner seedRooms(RoomTypeRepository types, RoomInformationRepository rooms) {
        return args -> seed(types, rooms);
    }

    @Transactional
    public void seed(RoomTypeRepository types, RoomInformationRepository rooms) {
        if (types.count() == 0) {
            types.saveAll(List.of(
                    type("Standard", "Comfortable room for everyday stays", "One or two guests"),
                    type("Deluxe", "Larger room with city view", "Up to three guests"),
                    type("Suite", "Suite with separate lounge", "Up to four guests")));
        }
        if (rooms.count() == 0) {
            List<RoomType> available = types.findAll();
            RoomType standard = available.get(0);
            RoomType deluxe = available.size() > 1 ? available.get(1) : standard;
            RoomType suite = available.size() > 2 ? available.get(2) : deluxe;
            rooms.saveAll(List.of(
                    room("101", "Quiet room with queen bed", 2, standard, "850000.00"),
                    room("102", "Twin beds near the garden", 2, standard, "850000.00"),
                    room("201", "City view and spacious work area", 3, deluxe, "1250000.00"),
                    room("202", "Balcony and king bed", 3, deluxe, "1350000.00"),
                    room("301", "Separate lounge and panoramic view", 4, suite, "2200000.00")));
        }
    }

    private RoomType type(String name, String description, String note) {
        RoomType type = new RoomType();
        type.roomTypeName = name;
        type.typeDescription = description;
        type.typeNote = note;
        return type;
    }

    private RoomInformation room(String number, String description, int capacity, RoomType type, String price) {
        RoomInformation room = new RoomInformation();
        room.roomNumber = number;
        room.roomDetailDescription = description;
        room.roomMaxCapacity = capacity;
        room.roomType = type;
        room.roomStatus = RoomStatus.ACTIVE;
        room.roomPricePerDay = new BigDecimal(price);
        return room;
    }
}
