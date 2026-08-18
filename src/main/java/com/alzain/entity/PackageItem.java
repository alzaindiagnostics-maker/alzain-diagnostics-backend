package com.alzain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String detailedDescription;

    @Column(name = "original_price")
    private Double originalPrice;

    @Column(name = "offer_price", nullable = false)
    private Double offerPrice;

    @Column(name = "discount_percentage")
    private Integer discountPercentage;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private Boolean featured;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "home_collection_available", nullable = false)
    private Boolean homeCollectionAvailable;

    @Column(name = "parameters_text", length = 100)
    private String parametersText;

    @Column(name = "preparation_instructions", columnDefinition = "TEXT")
    private String preparationInstructions;

    @Column(name = "report_information", columnDefinition = "TEXT")
    private String reportInformation;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "package_tests",
        joinColumns = @JoinColumn(name = "package_id"),
        inverseJoinColumns = @JoinColumn(name = "test_id")
    )
    @Builder.Default
    private List<TestItem> testItems = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "package_test_names", joinColumns = @JoinColumn(name = "package_id"))
    @Column(name = "test_name")
    @Builder.Default
    private List<String> testNames = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.featured = this.featured != null ? this.featured : false;
        this.active = this.active != null ? this.active : true;
        this.homeCollectionAvailable = this.homeCollectionAvailable != null ? this.homeCollectionAvailable : true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
