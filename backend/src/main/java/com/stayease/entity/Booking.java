package com.stayease.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="bookings",indexes={@Index(name="idx_booking_room_dates_status",columnList="room_id,check_in,check_out,status"),@Index(name="idx_booking_reference_email",columnList="booking_reference,customer_email")})
public class Booking {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true,length=32) private String bookingReference;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="room_id",nullable=false) private Room room;
    @Column(nullable=false,length=160) private String customerName;
    @Column(nullable=false,length=254) private String customerEmail;
    @Column(name="check_in",nullable=false) private LocalDate checkIn;
    @Column(name="check_out",nullable=false) private LocalDate checkOut;
    @Column(nullable=false) private int numberOfGuests;
    @Column(nullable=false,precision=12,scale=2) private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private BookingStatus status;
    @Column(nullable=false) private LocalDateTime createdAt;
    public Booking() {}
    public Long getId(){return id;} public String getBookingReference(){return bookingReference;} public void setBookingReference(String v){bookingReference=v;}
    public Room getRoom(){return room;} public void setRoom(Room v){room=v;}
    public String getCustomerName(){return customerName;} public void setCustomerName(String v){customerName=v;}
    public String getCustomerEmail(){return customerEmail;} public void setCustomerEmail(String v){customerEmail=v;}
    public LocalDate getCheckIn(){return checkIn;} public void setCheckIn(LocalDate v){checkIn=v;}
    public LocalDate getCheckOut(){return checkOut;} public void setCheckOut(LocalDate v){checkOut=v;}
    public int getNumberOfGuests(){return numberOfGuests;} public void setNumberOfGuests(int v){numberOfGuests=v;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
    public BookingStatus getStatus(){return status;} public void setStatus(BookingStatus v){status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
