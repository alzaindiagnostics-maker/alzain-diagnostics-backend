package com.alzain.controller;

import com.alzain.dto.BookingResponseDTO;
import com.alzain.dto.UpdateBookingStatusDTO;
import com.alzain.service.AdminBookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/bookings")
@CrossOrigin(origins = "*")
public class AdminBookingController {

    @Autowired
    private AdminBookingService adminBookingService;

    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings(@RequestParam(required = false) String status) {
        List<BookingResponseDTO> bookings = adminBookingService.getAllBookingsAdmin(status);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Long id) {
        BookingResponseDTO booking = adminBookingService.getBookingByIdAdmin(id);
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BookingResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusDTO dto) {
        BookingResponseDTO updated = adminBookingService.updateBookingStatus(id, dto.getStatus());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/notifications/{notificationId}/retry")
    public ResponseEntity<Map<String, Object>> retryNotification(@PathVariable Long notificationId) {
        boolean success = adminBookingService.retryNotification(notificationId);
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Notification resent successfully via SMTP." : "Notification retry failed. Check SMTP configuration and backend logs."
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        adminBookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
