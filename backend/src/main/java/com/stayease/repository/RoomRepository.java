package com.stayease.repository;
import com.stayease.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface RoomRepository extends JpaRepository<Room,String> {
    List<Room> findByHotelId(String hotelId);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from Room r where r.id=:id") Optional<Room> findByIdForUpdate(@Param("id") String id);
}
