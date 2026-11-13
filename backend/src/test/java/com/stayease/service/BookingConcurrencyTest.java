package com.stayease.service;

import com.stayease.dto.BookingRequest;
import com.stayease.entity.Hotel;
import com.stayease.entity.Room;
import com.stayease.exception.ApiException;
import com.stayease.repository.BookingRepository;
import com.stayease.repository.HotelRepository;
import com.stayease.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest @ActiveProfiles("test")
class BookingConcurrencyTest {
    @Autowired BookingService service;
    @Autowired HotelRepository hotels;
    @Autowired RoomRepository rooms;
    @Autowired BookingRepository bookings;

    @BeforeEach void setUp(){bookings.deleteAll();rooms.deleteAll();hotels.deleteAll();Hotel h=new Hotel();h.setId("race-hotel");h.setName("Concurrency Inn");h.setLocation("Pune");h.setArea("Pune");h.setDescription("Test inventory");h.setImageUrl("https://example.com/stay.jpg");h.setRating(4.5);h.setReviews(1);h.setBadge("Test");h.setAmenities(List.of("Breakfast"));hotels.saveAndFlush(h);Room r=new Room();r.setId("race-room");r.setHotel(h);r.setRoomType("Standard Room");r.setPricePerNight(new BigDecimal("10000.00"));r.setTotalRooms(1);r.setCapacity(2);rooms.saveAndFlush(r);}

    @Test void twoSimultaneousRequestsCannotOverbookTheLastRoom() throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch ready=new CountDownLatch(2),start=new CountDownLatch(1);LocalDate in=LocalDate.now().plusDays(20);
        Callable<String> request=()->{ready.countDown();start.await();try{service.create(new BookingRequest("race-hotel","Standard Room",null,"Guest","guest@example.com",in,in.plusDays(1),1));return "confirmed";}catch(ApiException ex){return "unavailable";}};
        Future<String> first=pool.submit(request),second=pool.submit(request);
        try { ready.await(5,TimeUnit.SECONDS);start.countDown();List<String> outcomes=List.of(first.get(10,TimeUnit.SECONDS),second.get(10,TimeUnit.SECONDS));assertEquals(1,outcomes.stream().filter("confirmed"::equals).count());assertEquals(1,outcomes.stream().filter("unavailable"::equals).count());assertEquals(1,bookings.count()); }
        finally {pool.shutdownNow();}
    }
}
