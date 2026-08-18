package com.alzain.service;

import com.alzain.dto.BookingResponseDTO;
import com.alzain.entity.Booking;
import com.alzain.exception.ResourceNotFoundException;
import com.alzain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBookingService {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final EmailNotificationService emailNotificationService;

    public List<BookingResponseDTO> getAllBookingsAdmin(String status) {
        List<Booking> bookings;
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            bookings = bookingRepository.findByStatus(status.trim().toUpperCase());
        } else {
            bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        }

        return bookings.stream()
                .map(bookingService::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public BookingResponseDTO getBookingByIdAdmin(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return bookingService.mapToResponseDTO(booking);
    }

    @Transactional
    public BookingResponseDTO updateBookingStatus(Long id, String newStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        String oldStatus = booking.getStatus();
        String formattedNewStatus = newStatus.trim().toUpperCase();

        booking.setStatus(formattedNewStatus);
        booking.setUpdatedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);
        log.info("Booking status updated: ID={}, BookingId={}, OldStatus={}, NewStatus={}", saved.getId(), saved.getBookingId(), oldStatus, formattedNewStatus);

        // Trigger customer email notification on status change (non-blocking)
        if (!formattedNewStatus.equalsIgnoreCase(oldStatus)) {
            try {
                if ("CONFIRMED".equalsIgnoreCase(formattedNewStatus)) {
                    emailNotificationService.sendCustomerBookingConfirmedNotification(saved);
                } else if ("CANCELLED".equalsIgnoreCase(formattedNewStatus)) {
                    emailNotificationService.sendCustomerBookingCancelledNotification(saved);
                }
            } catch (Exception e) {
                log.error("Error triggering status change notification for booking {}: {}", saved.getBookingId(), e.getMessage());
            }
        }

        return bookingService.mapToResponseDTO(saved);
    }

    @Transactional
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Booking not found with id: " + id);
        }
        bookingRepository.deleteById(id);
    }

    public boolean retryNotification(Long notificationId) {
        return emailNotificationService.retryNotification(notificationId);
    }
}
