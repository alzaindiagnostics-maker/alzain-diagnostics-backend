package com.alzain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false, unique = true, length = 30)
    private String bookingId;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "package_id")
    private Long packageId;

    @Column(name = "package_name", nullable = false, length = 150)
    private String packageName;

    @Column(name = "preferred_date", nullable = false, length = 30)
    private String preferredDate;

    @Column(name = "preferred_time", length = 50)
    private String preferredTime;

    @Column(name = "is_home_collection", nullable = false)
    private Boolean isHomeCollection;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 50)
    private String city;

    @Column(length = 10)
    private String pincode;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "payment_status", nullable = false, length = 30)
    private String paymentStatus; // PENDING, PAID, FAILED, REFUNDED


    @Column(nullable = false, length = 30)
    private String status; // PENDING, CONFIRMED, SAMPLE_COLLECTED, COMPLETED, CANCELLED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = "PENDING";
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = "PENDING";
        }
        if (this.isHomeCollection == null) {
            this.isHomeCollection = true;
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
