package com.alzain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageRequestDTO {

    @NotBlank(message = "Package name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    private String shortDescription;

    private String detailedDescription;

    @NotNull(message = "Original price is required")
    @Positive(message = "Original price must be greater than zero")
    private Double originalPrice;

    @NotNull(message = "Offer price is required")
    @Positive(message = "Offer price must be greater than zero")
    private Double offerPrice;

    private Integer discountPercentage;

    private String parametersText;

    private String preparationInstructions;

    private String reportInformation;

    private String imageUrl;

    private Boolean active;

    private Boolean featured;

    private Boolean homeCollectionAvailable;

    private List<String> testNames;
}
