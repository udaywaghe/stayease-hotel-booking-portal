package com.stayease.dto;
import java.math.BigDecimal;
import java.util.List;
public record HotelResponse(String id,String name,String location,String area,String description,String image,String imageUrl,double rating,int reviews,String badge,List<String> amenities,BigDecimal price,List<RoomResponse> rooms) {}
