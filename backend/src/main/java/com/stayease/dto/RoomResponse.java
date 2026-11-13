package com.stayease.dto;
import java.math.BigDecimal;
public record RoomResponse(String id,String name,int capacity,BigDecimal price,int totalRooms) {}
