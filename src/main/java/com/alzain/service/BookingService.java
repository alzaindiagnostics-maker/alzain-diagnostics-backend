package com.alzain.service;

import com.alzain.dto.BookingRequestDTO;
import com.alzain.dto.BookingResponseDTO;
import com.alzain.dto.NotificationDTO;
import com.alzain.entity.Booking;
import com.alzain.entity.Notification;
import com.alzain.repository.BookingRepository;
import com.alzain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final NotificationRepository notificationRepository;
    private final EmailNotificationService emailNotificationService;

    private static final String ALPHA_NUMERIC = "123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom random = new SecureRandom();

    public BookingResponseDTO createBooking(BookingRequestDTO dto) {
        String uniqueBookingId = generateUniqueBookingId();

        boolean homeCollection = dto.getIsHomeCollection() != null ? dto.getIsHomeCollection() : true;
        String city = (dto.getCity() != null && !dto.getCity().trim().isEmpty()) ? dto.getCity() : "Pullampet";
        String pincode = (dto.getPincode() != null && !dto.getPincode().trim().isEmpty()) ? dto.getPincode() : "516107";

        Booking booking = Booking.builder()
                .bookingId(uniqueBookingId)
                .customerName(dto.getCustomerName().trim())
                .phone(dto.getPhone().trim())
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .address(dto.getAddress() != null ? dto.getAddress().trim() : "")
                .city(city)
                .pincode(pincode)
                .preferredDate(dto.getPreferredDate().trim())
                .preferredTime(dto.getPreferredTime().trim())
                .packageId(dto.getPackageId())
                .packageName(dto.getPackageName().trim())
                .isHomeCollection(homeCollection)
                .status("PENDING")
                .message(dto.getMessage() != null ? dto.getMessage().trim() : null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Booking saved successfully to Database: ID={}, BookingId={}, Status=PENDING", saved.getId(), saved.getBookingId());

        // Send admin notification AFTER successful DB save (catch any SMTP errors inside service)
        try {
            emailNotificationService.sendAdminNewBookingNotification(saved);
        } catch (Exception e) {
            log.error("Non-blocking error during admin notification for booking {}: {}", saved.getBookingId(), e.getMessage());
        }

        return mapToResponseDTO(saved);
    }

    public Optional<BookingResponseDTO> getBookingByBookingId(String bookingId) {
        return bookingRepository.findByBookingId(bookingId.trim().toUpperCase())
                .map(this::mapToResponseDTO);
    }

    private String generateUniqueBookingId() {
        int currentYear = Year.now().getValue();
        String candidate;
        do {
            StringBuilder sb = new StringBuilder("ALZ-");
            sb.append(currentYear).append("-");
            for (int i = 0; i < 4; i++) {
                sb.append(ALPHA_NUMERIC.charAt(random.nextInt(ALPHA_NUMERIC.length())));
            }
            candidate = sb.toString();
        } while (bookingRepository.existsByBookingId(candidate));

        return candidate;
    }

    public BookingResponseDTO mapToResponseDTO(Booking booking) {
        List<Notification> notifications = notificationRepository.findByBookingIdOrderByCreatedAtDesc(booking.getId());
        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(n -> NotificationDTO.builder()
                        .id(n.getId())
                        .bookingId(booking.getId())
                        .recipient(n.getRecipient())
                        .notificationType(n.getNotificationType())
                        .status(n.getStatus())
                        .failureReason(n.getFailureReason())
                        .sentAt(n.getSentAt())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        String adminStatus = notifications.stream()
                .filter(n -> "ADMIN_NEW_BOOKING".equals(n.getNotificationType()))
                .map(Notification::getStatus)
                .findFirst()
                .orElse("PENDING");

        String customerStatus = notifications.stream()
                .filter(n -> n.getNotificationType().startsWith("CUSTOMER_"))
                .map(Notification::getStatus)
                .findFirst()
                .orElse("NONE");

        return BookingResponseDTO.builder()
                .id(booking.getId())
                .bookingId(booking.getBookingId())
                .customerName(booking.getCustomerName())
                .phone(booking.getPhone())
                .email(booking.getEmail())
                .packageName(booking.getPackageName())
                .preferredDate(booking.getPreferredDate())
                .preferredTime(booking.getPreferredTime())
                .isHomeCollection(booking.getIsHomeCollection())
                .address(booking.getAddress())
                .city(booking.getCity())
                .pincode(booking.getPincode())
                .status(booking.getStatus())
                .message(booking.getMessage())
                .amount(booking.getAmount())
                .paymentStatus(booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "PENDING")
                .createdAt(booking.getCreatedAt())
                .notifications(notificationDTOs)
                .adminNotificationStatus(adminStatus)
                .customerNotificationStatus(customerStatus)
                .build();
    }
}
