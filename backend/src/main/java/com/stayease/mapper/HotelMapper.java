package com.stayease.mapper;
import com.stayease.dto.*;
import com.stayease.entity.*;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;
@Component
public class HotelMapper {
    public RoomResponse room(Room r){return new RoomResponse(r.getId(),r.getRoomType(),r.getCapacity(),r.getPricePerNight(),r.getTotalRooms());}
    public HotelResponse hotel(Hotel h,List<Room> rooms){BigDecimal price=rooms.stream().map(Room::getPricePerNight).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);return new HotelResponse(h.getId(),h.getName(),h.getLocation(),h.getArea(),h.getDescription(),h.getImageUrl(),h.getImageUrl(),h.getRating(),h.getReviews(),h.getBadge(),h.getAmenities(),price,rooms.stream().map(this::room).toList());}
    public BookingResponse booking(Booking b){Room r=b.getRoom();Hotel h=r.getHotel();long nights=java.time.temporal.ChronoUnit.DAYS.between(b.getCheckIn(),b.getCheckOut());String status=b.getStatus()==BookingStatus.CONFIRMED?"Confirmed":"Cancelled";return new BookingResponse(b.getBookingReference(),b.getBookingReference(),h.getId(),h.getName(),h.getLocation(),h.getImageUrl(),r.getId(),r.getRoomType(),r.getPricePerNight(),Math.toIntExact(nights),b.getCheckIn(),b.getCheckOut(),b.getNumberOfGuests(),b.getCustomerName(),b.getCustomerEmail(),b.getTotalAmount(),status,b.getCreatedAt());}
}
