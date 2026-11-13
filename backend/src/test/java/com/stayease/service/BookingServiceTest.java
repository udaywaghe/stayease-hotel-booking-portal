package com.stayease.service;
import com.stayease.entity.*;
import com.stayease.dto.BookingRequest;
import com.stayease.dto.BookingResponse;
import com.stayease.exception.ApiException;
import com.stayease.mapper.HotelMapper;
import com.stayease.repository.BookingRepository;
import com.stayease.repository.RoomRepository;
import com.stayease.mapper.HotelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock BookingRepository bookings; @Mock RoomRepository rooms; BookingService service;
    @BeforeEach void setUp(){service=new BookingService(bookings,rooms,new HotelMapper());}
    @Test void retrievalRequiresReferenceAndMatchingEmail(){when(bookings.findByBookingReferenceIgnoreCaseAndCustomerEmailIgnoreCase("SE-MISSING","nobody@example.com")).thenReturn(Optional.empty());assertThrows(ApiException.class,()->service.find("SE-MISSING","nobody@example.com"));}
    @Test void repeatedCancellationIsRejectedAfterInventoryLock(){Hotel h=new Hotel();h.setId("h1");Room room=new Room();room.setId("h1-r1");room.setHotel(h);Booking booking=new Booking();booking.setRoom(room);booking.setStatus(BookingStatus.CANCELLED);when(bookings.findForUpdate("SE-REPEAT","guest@example.com")).thenReturn(Optional.of(booking));when(rooms.findByIdForUpdate("h1-r1")).thenReturn(Optional.of(room));assertThrows(ApiException.class,()->service.cancel("SE-REPEAT","guest@example.com"));verify(bookings,never()).save(any());}
    @Test void rejectsGuestCountAboveRoomCapacity(){Room room=room();when(rooms.findByHotelId("h1")).thenReturn(List.of(room));when(rooms.findByIdForUpdate("h1-r1")).thenReturn(Optional.of(room));BookingRequest request=request(3);assertThrows(ApiException.class,()->service.create(request));verify(bookings,never()).countOverlapping(anyString(),any(),any(),any());}
    @Test void rejectsAnOverlappingRequestWhenInventoryIsFull(){Room room=room();when(rooms.findByHotelId("h1")).thenReturn(List.of(room));when(rooms.findByIdForUpdate("h1-r1")).thenReturn(Optional.of(room));when(bookings.countOverlapping(eq("h1-r1"),eq(BookingStatus.CONFIRMED),any(),any())).thenReturn(1L);assertThrows(ApiException.class,()->service.create(request(2)));}
    @Test void calculatesBackendTotalFromRoomPriceAndStayDates(){Room room=room();when(rooms.findByHotelId("h1")).thenReturn(List.of(room));when(rooms.findByIdForUpdate("h1-r1")).thenReturn(Optional.of(room));when(bookings.countOverlapping(eq("h1-r1"),eq(BookingStatus.CONFIRMED),any(),any())).thenReturn(0L);when(bookings.save(any())).thenAnswer(invocation->invocation.getArgument(0));BookingResponse result=service.create(request(2));assertEquals(new BigDecimal("85500.00"),result.total());assertEquals(3,result.nights());assertEquals("Confirmed",result.status());}
    private Room room(){Hotel h=new Hotel();h.setId("h1");h.setName("Leela");h.setLocation("Udaipur");h.setImageUrl("image");Room r=new Room();r.setId("h1-r1");r.setHotel(h);r.setRoomType("Palace Room");r.setCapacity(2);r.setTotalRooms(1);r.setPricePerNight(new BigDecimal("28500.00"));return r;}
    private BookingRequest request(int guests){LocalDate in=LocalDate.now().plusDays(8);return new BookingRequest("h1","Palace Room",null,"Guest","guest@example.com",in,in.plusDays(3),guests);}
}
