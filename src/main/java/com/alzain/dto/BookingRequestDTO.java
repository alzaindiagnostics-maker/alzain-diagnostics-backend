package com.alzain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDTO {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian mobile number")
    private String phone;

    private String email;

    private String address;

    private String city;

    private String pincode;

    @NotBlank(message = "Preferred date is required")
    private String preferredDate;

    @NotBlank(message = "Preferred time slot is required")
    private String preferredTime;

    private Long packageId;

    @NotBlank(message = "Package or test name is required")
    private String packageName;

    private Boolean isHomeCollection;

    private String message;
}
