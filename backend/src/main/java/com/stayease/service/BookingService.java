package com.stayease.service;

import com.stayease.dto.BookingRequest;
import com.stayease.dto.BookingResponse;
import com.stayease.entity.*;
import com.stayease.exception.ApiException;
import com.stayease.mapper.HotelMapper;
import com.stayease.repository.BookingRepository;
import com.stayease.repository.RoomRepository;
import com.stayease.util.BookingRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BookingService {
    private final BookingRepository bookings; private final RoomRepository rooms; private final HotelMapper mapper;
    public BookingService(BookingRepository bookings,RoomRepository rooms,HotelMapper mapper){this.bookings=bookings;this.rooms=rooms;this.mapper=mapper;}

    @Transactional(isolation=Isolation.READ_COMMITTED)
    public BookingResponse create(BookingRequest request){int nights=BookingRules.nights(request.checkIn(),request.checkOut());if(request.numberOfGuests()<1)throw ApiException.badRequest("Number of guests must be positive");
        Room requested=resolveRoom(request);Room room=rooms.findByIdForUpdate(requested.getId()).orElseThrow(()->ApiException.notFound("Room not found"));
        if(!room.getHotel().getId().equals(request.hotelId()))throw ApiException.badRequest("Room does not belong to the selected hotel");
        if(request.numberOfGuests()>room.getCapacity())throw ApiException.badRequest("Guest count exceeds room capacity");
        long occupied=bookings.countOverlapping(room.getId(),BookingStatus.CONFIRMED,request.checkIn(),request.checkOut());
        if(occupied>=room.getTotalRooms())throw ApiException.conflict("Room is unavailable for the selected dates");
        BigDecimal total=BookingRules.total(room.getPricePerNight(),nights);
        Booking booking=new Booking();booking.setBookingReference(newReference());booking.setRoom(room);booking.setCustomerName(request.customerName().trim());booking.setCustomerEmail(request.customerEmail().trim().toLowerCase());booking.setCheckIn(request.checkIn());booking.setCheckOut(request.checkOut());booking.setNumberOfGuests(request.numberOfGuests());booking.setTotalAmount(total);booking.setStatus(BookingStatus.CONFIRMED);booking.setCreatedAt(LocalDateTime.now());return mapper.booking(bookings.save(booking));
    }
    @Transactional(readOnly=true)
    public BookingResponse find(String reference,String email){if(email==null||email.isBlank())throw ApiException.badRequest("Email is required");return bookings.findByBookingReferenceIgnoreCaseAndCustomerEmailIgnoreCase(reference,email.trim()).map(mapper::booking).orElseThrow(()->ApiException.notFound("Booking reference and email were not found"));}
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public BookingResponse cancel(String reference,String email){if(email==null||email.isBlank())throw ApiException.badRequest("Email is required");Booking booking=bookings.findForUpdate(reference,email.trim()).orElseThrow(()->ApiException.notFound("Booking reference and email were not found"));Room room=rooms.findByIdForUpdate(booking.getRoom().getId()).orElseThrow(()->ApiException.notFound("Room not found"));if(booking.getStatus()==BookingStatus.CANCELLED)throw ApiException.conflict("Booking is already cancelled");booking.setRoom(room);booking.setStatus(BookingStatus.CANCELLED);return mapper.booking(bookings.save(booking));}
    private Room resolveRoom(BookingRequest request){if(request.roomId()!=null&&!request.roomId().isBlank())return rooms.findById(request.roomId()).orElseThrow(()->ApiException.notFound("Room not found"));if(request.roomType()!=null&&!request.roomType().isBlank())return rooms.findByHotelId(request.hotelId()).stream().filter(r->r.getRoomType().equalsIgnoreCase(request.roomType())).findFirst().orElseThrow(()->ApiException.notFound("Room category not found"));throw ApiException.badRequest("A room id or room category is required");}
    private String newReference(){return "SE-"+UUID.randomUUID().toString().replace("-","").substring(0,10).toUpperCase();}
}
