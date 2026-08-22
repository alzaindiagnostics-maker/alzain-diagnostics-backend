package com.alzain.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicBookingTrackDTO {
    private String bookingId;
    private String maskedCustomerName;
    private String maskedPhone;
    private String packageName;
    private String preferredDate;
    private String preferredTime;
    private Boolean isHomeCollection;
    private String city;
    private String status;
    private String paymentStatus;
    private LocalDateTime createdAt;
}
