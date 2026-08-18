package com.alzain.controller;

import com.alzain.dto.PackageRequestDTO;
import com.alzain.entity.PackageItem;
import com.alzain.service.AdminPackageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/packages")
@CrossOrigin(origins = "*")
public class AdminPackageController {

    @Autowired
    private AdminPackageService adminPackageService;

    @GetMapping
    public ResponseEntity<List<PackageItem>> getAllPackages() {
        return ResponseEntity.ok(adminPackageService.getAllPackagesAdmin());
    }

    @PostMapping
    public ResponseEntity<PackageItem> createPackage(@Valid @RequestBody PackageRequestDTO dto) {
        PackageItem created = adminPackageService.createPackage(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PackageItem> updatePackage(@PathVariable Long id, @Valid @RequestBody PackageRequestDTO dto) {
        PackageItem updated = adminPackageService.updatePackage(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<PackageItem> togglePackageStatus(@PathVariable Long id) {
        PackageItem updated = adminPackageService.togglePackageActive(id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        adminPackageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }
}
