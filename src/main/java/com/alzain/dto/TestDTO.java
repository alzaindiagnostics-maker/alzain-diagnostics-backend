package com.alzain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestDTO {

    private Long id;

    @NotBlank(message = "Test name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    private String shortDescription;

    private String detailedDescription;

    private Boolean active;
}
