package com.alzain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactEnquiryDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String email;

    private String service;

    @NotBlank(message = "Message is required")
    private String message;
}
