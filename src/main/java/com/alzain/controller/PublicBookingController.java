package com.alzain.controller;

import com.alzain.dto.BookingRequestDTO;
import com.alzain.dto.BookingResponseDTO;
import com.alzain.dto.PublicBookingTrackDTO;
import com.alzain.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/public/bookings")
@CrossOrigin(origins = "*")
@Slf4j
public class PublicBookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO dto) {
        log.info("Received public booking request for customer: {}, package: {}", dto.getCustomerName(), dto.getPackageName());
        BookingResponseDTO response = bookingService.createBooking(dto);
        log.info("Successfully processed booking. Assigned ID: {}", response.getBookingId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/track/{bookingId}")
    public ResponseEntity<PublicBookingTrackDTO> trackBooking(@PathVariable String bookingId) {
        return bookingService.getPublicBookingByBookingId(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
