package com.alzain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private Long bookingId;
    private String recipient;
    private String notificationType;
    private String status;
    private String failureReason;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
