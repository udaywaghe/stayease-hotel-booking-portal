package com.stayease.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
public record BookingResponse(String reference,String bookingReference,String hotelId,String hotelName,String hotelLocation,String hotelImage,String roomId,String roomName,BigDecimal price,int nights,LocalDate checkIn,LocalDate checkOut,int guests,String name,String email,BigDecimal total,String status,LocalDateTime createdAt) {}
