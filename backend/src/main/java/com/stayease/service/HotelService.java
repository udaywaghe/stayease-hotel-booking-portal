package com.stayease.service;

import com.stayease.dto.*;
import com.stayease.entity.*;
import com.stayease.exception.ApiException;
import com.stayease.mapper.HotelMapper;
import com.stayease.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class HotelService {
    private final HotelRepository hotels; private final RoomRepository rooms; private final BookingRepository bookings; private final HotelMapper mapper;
    public HotelService(HotelRepository hotels,RoomRepository rooms,BookingRepository bookings,HotelMapper mapper){this.hotels=hotels;this.rooms=rooms;this.bookings=bookings;this.mapper=mapper;}
    @Transactional(readOnly=true)
    public List<HotelResponse> list(String location,LocalDate checkIn,LocalDate checkOut){List<Hotel> found=location==null||location.isBlank()?hotels.findAll():hotels.findByLocationContainingIgnoreCaseOrAreaContainingIgnoreCase(location,location);if((checkIn==null)!=(checkOut==null))throw ApiException.badRequest("Both checkIn and checkOut are required");if(checkIn!=null)validateRange(checkIn,checkOut);return found.stream().map(h->mapper.hotel(h,rooms.findByHotelId(h.getId()))).filter(h->checkIn==null||availability(h.id(),checkIn,checkOut,1).available()).toList();}
    @Transactional(readOnly=true)
    public HotelResponse get(String id){Hotel h=hotels.findById(id).orElseThrow(()->ApiException.notFound("Hotel not found"));return mapper.hotel(h,rooms.findByHotelId(id));}
    @Transactional(readOnly=true)
    public List<HotelResponse> search(String location,LocalDate checkIn,LocalDate checkOut){if(location==null||location.isBlank())throw ApiException.badRequest("Location is required");return list(location,checkIn,checkOut);}
    @Transactional(readOnly=true)
    public AvailabilityResponse availability(String hotelId,LocalDate checkIn,LocalDate checkOut,int guests){validateRange(checkIn,checkOut);if(guests<1)throw ApiException.badRequest("Number of guests must be positive");if(!hotels.existsById(hotelId))throw ApiException.notFound("Hotel not found");List<RoomResponse> available=rooms.findByHotelId(hotelId).stream().filter(r->r.getCapacity()>=guests&&bookings.countOverlapping(r.getId(),BookingStatus.CONFIRMED,checkIn,checkOut)<r.getTotalRooms()).map(mapper::room).toList();return new AvailabilityResponse(hotelId,checkIn,checkOut,guests,!available.isEmpty(),available);}
    private void validateRange(LocalDate in,LocalDate out){if(in==null||out==null)throw ApiException.badRequest("Check-in and check-out are required");if(in.isBefore(LocalDate.now()))throw ApiException.badRequest("Check-in cannot be in the past");if(!out.isAfter(in))throw ApiException.badRequest("Check-out must be after check-in");}
}
