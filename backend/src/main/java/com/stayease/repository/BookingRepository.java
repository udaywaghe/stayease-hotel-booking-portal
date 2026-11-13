package com.stayease.repository;
import com.stayease.entity.Booking;
import com.stayease.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.Optional;
public interface BookingRepository extends JpaRepository<Booking,Long> {
    @Query("select count(b) from Booking b where b.room.id=:roomId and b.status=:status and b.checkIn<:checkOut and b.checkOut>:checkIn")
    long countOverlapping(@Param("roomId") String roomId,@Param("status") BookingStatus status,@Param("checkIn") LocalDate checkIn,@Param("checkOut") LocalDate checkOut);
    Optional<Booking> findByBookingReferenceIgnoreCaseAndCustomerEmailIgnoreCase(String reference,String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select b from Booking b where lower(b.bookingReference)=lower(:reference) and lower(b.customerEmail)=lower(:email)")
    Optional<Booking> findForUpdate(@Param("reference") String reference,@Param("email") String email);
}
