package com.stayease.controller;
import com.stayease.dto.BookingRequest;
import com.stayease.dto.BookingResponse;
import com.stayease.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service; public BookingController(BookingService service){this.service=service;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public BookingResponse create(@Valid @RequestBody BookingRequest request){return service.create(request);}
    @GetMapping("/{reference}") public BookingResponse get(@PathVariable String reference,@RequestParam String email){return service.find(reference,email);}
    @PostMapping("/{reference}/cancel") public BookingResponse cancel(@PathVariable String reference,@RequestParam String email){return service.cancel(reference,email);}
}
