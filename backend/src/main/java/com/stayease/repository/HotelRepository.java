package com.stayease.repository;
import com.stayease.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface HotelRepository extends JpaRepository<Hotel,String> { List<Hotel> findByLocationContainingIgnoreCaseOrAreaContainingIgnoreCase(String location,String area); }
