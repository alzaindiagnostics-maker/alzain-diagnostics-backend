package com.alzain.controller;

import com.alzain.dto.ContactEnquiryDTO;
import com.alzain.entity.PackageItem;
import com.alzain.service.BusinessSettingService;
import com.alzain.service.EmailNotificationService;
import com.alzain.service.PackageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class PublicPackageController {

    @Autowired
    private PackageService packageService;

    @Autowired
    private BusinessSettingService businessSettingService;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @GetMapping("/packages")
    public ResponseEntity<List<PackageItem>> getPackages(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort) {
        List<PackageItem> packages = packageService.getAllActivePackages(query, category, sort);
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/packages/featured")
    public ResponseEntity<List<PackageItem>> getFeaturedPackages() {
        return ResponseEntity.ok(packageService.getFeaturedPackages());
    }

    @GetMapping("/packages/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(packageService.getAllCategories());
    }

    @GetMapping("/packages/{slug}")
    public ResponseEntity<PackageItem> getPackageBySlug(@PathVariable String slug) {
        return packageService.getPackageBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, String>> getBusinessSettings() {
        return ResponseEntity.ok(businessSettingService.getAllSettingsAsMap());
    }

    @PostMapping("/contact")
    public ResponseEntity<Map<String, Object>> submitContactEnquiry(@Valid @RequestBody ContactEnquiryDTO dto) {
        emailNotificationService.sendContactEnquiryNotification(
                dto.getName().trim(),
                dto.getPhone().trim(),
                dto.getEmail() != null ? dto.getEmail().trim() : null,
                dto.getService(),
                dto.getMessage().trim());
        return ResponseEntity.ok(Map.of("success", true, "message", "Enquiry received successfully"));
    }
}
