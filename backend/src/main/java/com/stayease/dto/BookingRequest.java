package com.stayease.dto;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record BookingRequest(@NotBlank String hotelId,@JsonAlias("roomName") String roomType,String roomId,@JsonAlias("name") @NotBlank @Size(max=160) String customerName,@JsonAlias("email") @NotBlank @Email @Size(max=254) String customerEmail,@NotNull LocalDate checkIn,@NotNull LocalDate checkOut,@JsonAlias("guests") @Min(1) int numberOfGuests) {}
