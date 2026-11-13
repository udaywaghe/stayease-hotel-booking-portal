package com.stayease.dto;
import java.time.LocalDate;
import java.util.List;
public record AvailabilityResponse(String hotelId,LocalDate checkIn,LocalDate checkOut,int guests,boolean available,List<RoomResponse> rooms) {}
