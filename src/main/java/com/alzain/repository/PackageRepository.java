package com.alzain.repository;

import com.alzain.entity.PackageItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<PackageItem, Long> {
    Optional<PackageItem> findBySlug(String slug);
    List<PackageItem> findByActiveTrue();
    List<PackageItem> findByFeaturedTrueAndActiveTrue();
    List<PackageItem> findByCategoryAndActiveTrue(String category);
    Boolean existsBySlug(String slug);
    long countByActiveTrue();
}
