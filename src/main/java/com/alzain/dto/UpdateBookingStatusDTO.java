package com.alzain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookingStatusDTO {

    @NotBlank(message = "Status is required")
    private String status;

    private String notes;
}
