package com.alzain.service;

import com.alzain.entity.PackageItem;
import com.alzain.repository.PackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PackageService {

    @Autowired
    private PackageRepository packageRepository;

    public List<PackageItem> getAllActivePackages(String query, String category, String sort) {
        List<PackageItem> packages = packageRepository.findByActiveTrue();

        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            packages = packages.stream()
                    .filter(p -> category.equalsIgnoreCase(p.getCategory()))
                    .collect(Collectors.toList());
        }

        if (query != null && !query.trim().isEmpty()) {
            String q = query.toLowerCase().trim();
            packages = packages.stream()
                    .filter(p -> p.getName().toLowerCase().contains(q) ||
                            (p.getShortDescription() != null && p.getShortDescription().toLowerCase().contains(q)) ||
                            (p.getParametersText() != null && p.getParametersText().toLowerCase().contains(q)) ||
                            (p.getTestNames() != null && p.getTestNames().stream().anyMatch(t -> t.toLowerCase().contains(q))))
                    .collect(Collectors.toList());
        }

        if (sort != null) {
            switch (sort.toLowerCase()) {
                case "price-low":
                    packages.sort(Comparator.comparingDouble(PackageItem::getOfferPrice));
                    break;
                case "price-high":
                    packages.sort(Comparator.comparingDouble(PackageItem::getOfferPrice).reversed());
                    break;
                case "discount":
                    packages.sort(Comparator.comparingInt((PackageItem p) -> 
                            p.getDiscountPercentage() != null ? p.getDiscountPercentage() : 0).reversed());
                    break;
                default:
                    // Default sorting: featured first, then by ID
                    packages.sort((p1, p2) -> Boolean.compare(
                            Boolean.TRUE.equals(p2.getFeatured()),
                            Boolean.TRUE.equals(p1.getFeatured())
                    ));
                    break;
            }
        }

        return packages;
    }

    public Optional<PackageItem> getPackageBySlug(String slug) {
        return packageRepository.findBySlug(slug);
    }

    public List<String> getAllCategories() {
        List<PackageItem> packages = packageRepository.findByActiveTrue();
        return packages.stream()
                .map(PackageItem::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<PackageItem> getFeaturedPackages() {
        return packageRepository.findByActiveTrue().stream()
                .filter(p -> Boolean.TRUE.equals(p.getFeatured()))
                .collect(Collectors.toList());
    }
}
