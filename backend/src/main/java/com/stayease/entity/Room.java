package com.stayease.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="rooms",indexes=@Index(name="idx_rooms_hotel",columnList="hotel_id"))
public class Room {
    @Id @Column(length=40) private String id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="hotel_id",nullable=false) private Hotel hotel;
    @Column(nullable=false,length=120) private String roomType;
    @Column(nullable=false,precision=12,scale=2) private BigDecimal pricePerNight;
    @Column(nullable=false) private int totalRooms;
    @Column(nullable=false) private int capacity;
    @Version private long version;
    public Room() {}
    public String getId(){return id;} public void setId(String v){id=v;}
    public Hotel getHotel(){return hotel;} public void setHotel(Hotel v){hotel=v;}
    public String getRoomType(){return roomType;} public void setRoomType(String v){roomType=v;}
    public BigDecimal getPricePerNight(){return pricePerNight;} public void setPricePerNight(BigDecimal v){pricePerNight=v;}
    public int getTotalRooms(){return totalRooms;} public void setTotalRooms(int v){totalRooms=v;}
    public int getCapacity(){return capacity;} public void setCapacity(int v){capacity=v;}
}
