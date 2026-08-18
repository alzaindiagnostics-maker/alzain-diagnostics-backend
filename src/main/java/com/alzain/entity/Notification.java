package com.alzain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = true)
    private Booking booking;

    @Column(name = "recipient", nullable = false, length = 150)
    private String recipient;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType; // ADMIN_NEW_BOOKING, CUSTOMER_BOOKING_CONFIRMED, CUSTOMER_BOOKING_CANCELLED, ADMIN_PASSWORD_RESET

    @Column(name = "status", nullable = false, length = 20)
    private String status; // PENDING, SENT, FAILED

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "resend_email_id", length = 100)
    private String resendEmailId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }
}
