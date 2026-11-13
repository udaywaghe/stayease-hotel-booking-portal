package com.stayease.controller;
import com.stayease.dto.*;
import com.stayease.service.HotelService;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController @RequestMapping("/api/hotels") @Validated
public class HotelController {
    private final HotelService service; public HotelController(HotelService service){this.service=service;}
    @GetMapping public List<HotelResponse> list(@RequestParam(required=false) String location,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate checkIn,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate checkOut){return service.list(location,checkIn,checkOut);}
    @GetMapping("/search") public List<HotelResponse> search(@RequestParam String location,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate checkIn,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate checkOut){return service.search(location,checkIn,checkOut);}
    @GetMapping("/{id}") public HotelResponse get(@PathVariable String id){return service.get(id);}
    @GetMapping("/{id}/availability") public AvailabilityResponse availability(@PathVariable String id,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate checkIn,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate checkOut,@RequestParam(defaultValue="1") @Min(1) int guests){return service.availability(id,checkIn,checkOut,guests);}
}
