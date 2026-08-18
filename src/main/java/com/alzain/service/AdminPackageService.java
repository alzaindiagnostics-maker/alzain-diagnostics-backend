package com.alzain.service;

import com.alzain.dto.PackageRequestDTO;
import com.alzain.entity.PackageItem;
import com.alzain.exception.ResourceNotFoundException;
import com.alzain.repository.PackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AdminPackageService {

    @Autowired
    private PackageRepository packageRepository;

    public List<PackageItem> getAllPackagesAdmin() {
        return packageRepository.findAll();
    }

    @Transactional
    public PackageItem createPackage(PackageRequestDTO dto) {
        String slug = generateSlug(dto.getName());
        int discount = calculateDiscount(dto.getOriginalPrice(), dto.getOfferPrice(), dto.getDiscountPercentage());

        PackageItem pkg = PackageItem.builder()
                .name(dto.getName().trim())
                .category(dto.getCategory().trim())
                .shortDescription(dto.getShortDescription())
                .detailedDescription(dto.getDetailedDescription())
                .originalPrice(dto.getOriginalPrice())
                .offerPrice(dto.getOfferPrice())
                .discountPercentage(discount)
                .parametersText(dto.getParametersText() != null ? dto.getParametersText() : "")
                .preparationInstructions(dto.getPreparationInstructions())
                .reportInformation(dto.getReportInformation())
                .imageUrl(dto.getImageUrl() != null ? dto.getImageUrl() : "/assets/packages/default.jpg")
                .active(dto.getActive() != null ? dto.getActive() : true)
                .featured(dto.getFeatured() != null ? dto.getFeatured() : false)
                .homeCollectionAvailable(dto.getHomeCollectionAvailable() != null ? dto.getHomeCollectionAvailable() : true)
                .slug(slug)
                .testNames(dto.getTestNames() != null ? dto.getTestNames() : new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return packageRepository.save(pkg);
    }

    @Transactional
    public PackageItem updatePackage(Long id, PackageRequestDTO dto) {
        PackageItem pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + id));

        if (!pkg.getName().equalsIgnoreCase(dto.getName())) {
            pkg.setSlug(generateSlug(dto.getName()));
        }

        int discount = calculateDiscount(dto.getOriginalPrice(), dto.getOfferPrice(), dto.getDiscountPercentage());

        pkg.setName(dto.getName().trim());
        pkg.setCategory(dto.getCategory().trim());
        pkg.setShortDescription(dto.getShortDescription());
        pkg.setDetailedDescription(dto.getDetailedDescription());
        pkg.setOriginalPrice(dto.getOriginalPrice());
        pkg.setOfferPrice(dto.getOfferPrice());
        pkg.setDiscountPercentage(discount);
        pkg.setParametersText(dto.getParametersText());
        pkg.setPreparationInstructions(dto.getPreparationInstructions());
        pkg.setReportInformation(dto.getReportInformation());
        if (dto.getImageUrl() != null) pkg.setImageUrl(dto.getImageUrl());
        if (dto.getActive() != null) pkg.setActive(dto.getActive());
        if (dto.getFeatured() != null) pkg.setFeatured(dto.getFeatured());
        if (dto.getHomeCollectionAvailable() != null) pkg.setHomeCollectionAvailable(dto.getHomeCollectionAvailable());
        if (dto.getTestNames() != null) pkg.setTestNames(dto.getTestNames());
        pkg.setUpdatedAt(LocalDateTime.now());

        return packageRepository.save(pkg);
    }

    @Transactional
    public PackageItem togglePackageActive(Long id) {
        PackageItem pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + id));
        pkg.setActive(!pkg.getActive());
        pkg.setUpdatedAt(LocalDateTime.now());
        return packageRepository.save(pkg);
    }

    @Transactional
    public void deletePackage(Long id) {
        if (!packageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Package not found with id: " + id);
        }
        packageRepository.deleteById(id);
    }

    private String generateSlug(String name) {
        String base = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");

        String candidate = base;
        int count = 1;
        while (packageRepository.existsBySlug(candidate)) {
            candidate = base + "-" + count++;
        }
        return candidate;
    }

    private int calculateDiscount(Double original, Double offer, Integer providedDiscount) {
        if (providedDiscount != null && providedDiscount > 0) return providedDiscount;
        if (original != null && offer != null && original > offer) {
            return (int) Math.round(((original - offer) / original) * 100);
        }
        return 0;
    }
}
