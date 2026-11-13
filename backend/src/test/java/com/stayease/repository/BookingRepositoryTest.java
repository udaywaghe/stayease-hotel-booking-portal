package com.stayease.repository;
import com.stayease.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest @ActiveProfiles("test")
class BookingRepositoryTest {
    @Autowired HotelRepository hotels; @Autowired RoomRepository rooms; @Autowired BookingRepository bookings;
    @Autowired PlatformTransactionManager transactionManager;
    @PersistenceContext EntityManager entityManager;
    @Test void countsOnlyConfirmedOverlappingBookingsAndHonorsCheckoutBoundary(){Hotel h=new Hotel();h.setId("test-hotel");h.setName("Test");h.setLocation("Test city");h.setArea("Test");h.setDescription("desc");h.setImageUrl("image");h.setRating(4);h.setReviews(1);h.setBadge("Test");h.setAmenities(List.of());hotels.save(h);Room r=new Room();r.setId("test-room");r.setHotel(h);r.setRoomType("Room");r.setPricePerNight(new BigDecimal("100"));r.setCapacity(2);r.setTotalRooms(1);rooms.save(r);
        LocalDate base=LocalDate.now().plusDays(10);Booking b=new Booking();b.setBookingReference("SE-TEST1");b.setRoom(r);b.setCustomerName("Guest");b.setCustomerEmail("guest@example.com");b.setCheckIn(base);b.setCheckOut(base.plusDays(2));b.setNumberOfGuests(1);b.setTotalAmount(new BigDecimal("200"));b.setStatus(BookingStatus.CONFIRMED);b.setCreatedAt(LocalDateTime.now());bookings.saveAndFlush(b);
        assertEquals(1,bookings.countOverlapping(r.getId(),BookingStatus.CONFIRMED,base.plusDays(1),base.plusDays(3)));
        assertEquals(0,bookings.countOverlapping(r.getId(),BookingStatus.CONFIRMED,base.plusDays(2),base.plusDays(4)));
        b.setStatus(BookingStatus.CANCELLED);bookings.saveAndFlush(b);assertEquals(0,bookings.countOverlapping(r.getId(),BookingStatus.CONFIRMED,base,base.plusDays(1)));
    }
    @Test void roomForeignKeyRejectsUnknownHotel(){Hotel unknown=new Hotel();unknown.setId("missing-hotel");Room room=new Room();room.setId("orphan-room");room.setHotel(unknown);room.setRoomType("Orphan");room.setPricePerNight(new BigDecimal("100"));room.setTotalRooms(1);room.setCapacity(2);assertThrows(RuntimeException.class,()->rooms.saveAndFlush(room));}
    @Test void hotelAmenitiesAndRoomsRoundTripThroughTheirMappedColumns(){Hotel h=hotel("roundtrip-hotel");hotels.saveAndFlush(h);Room r=room("roundtrip-room",h);rooms.saveAndFlush(r);entityManager.clear();Hotel loaded=hotels.findById("roundtrip-hotel").orElseThrow();Room loadedRoom=rooms.findById("roundtrip-room").orElseThrow();assertEquals(List.of("Breakfast"),loaded.getAmenities());assertEquals("roundtrip-hotel",loadedRoom.getHotel().getId());}
    @Test void bookingReferenceMustBeUnique(){Hotel h=hotel("unique-hotel");hotels.save(h);Room r=room("unique-room",h);rooms.save(r);LocalDate in=LocalDate.now().plusDays(5);bookings.saveAndFlush(booking("SE-DUPLICATE",r,in,in.plusDays(1)));assertThrows(RuntimeException.class,()->bookings.saveAndFlush(booking("SE-DUPLICATE",r,in.plusDays(2),in.plusDays(3))));}
    @Test @Transactional(propagation=Propagation.NOT_SUPPORTED) void failedTransactionRollsBackHotelInsert(){TransactionTemplate tx=new TransactionTemplate(transactionManager);tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);assertThrows(IllegalStateException.class,()->tx.execute(status->{hotels.saveAndFlush(hotel("rollback-hotel"));throw new IllegalStateException("rollback test");}));assertFalse(hotels.existsById("rollback-hotel"));}
    private Hotel hotel(String id){Hotel h=new Hotel();h.setId(id);h.setName("Test Hotel");h.setLocation("Test City");h.setArea("Test City");h.setDescription("Test description");h.setImageUrl("https://example.com/hotel.jpg");h.setRating(4.5);h.setReviews(10);h.setBadge("Test");h.setAmenities(List.of("Breakfast"));return h;}
    private Room room(String id,Hotel hotel){Room r=new Room();r.setId(id);r.setHotel(hotel);r.setRoomType("Test Room");r.setPricePerNight(new BigDecimal("12000.00"));r.setTotalRooms(2);r.setCapacity(2);return r;}
    private Booking booking(String reference,Room room,LocalDate in,LocalDate out){Booking b=new Booking();b.setBookingReference(reference);b.setRoom(room);b.setCustomerName("Guest");b.setCustomerEmail("guest@example.com");b.setCheckIn(in);b.setCheckOut(out);b.setNumberOfGuests(1);b.setTotalAmount(new BigDecimal("12000.00"));b.setStatus(BookingStatus.CONFIRMED);b.setCreatedAt(LocalDateTime.now());return b;}
}
