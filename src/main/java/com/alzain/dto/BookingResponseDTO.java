package com.alzain.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {

    private Long id;
    private String bookingId;
    private String customerName;
    private String phone;
    private String email;
    private String packageName;
    private String preferredDate;
    private String preferredTime;
    private Boolean isHomeCollection;
    private String address;
    private String city;
    private String pincode;
    private String status;
    private String message;
    private Double amount;
    private String paymentStatus; // PENDING, PAID, FAILED, REFUNDED
    private LocalDateTime createdAt;

    private List<NotificationDTO> notifications;
    private String adminNotificationStatus; // SENT, FAILED, PENDING
    private String customerNotificationStatus; // SENT, FAILED, PENDING, NONE
}
