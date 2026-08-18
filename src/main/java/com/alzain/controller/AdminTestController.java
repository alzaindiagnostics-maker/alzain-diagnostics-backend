package com.alzain.controller;

import com.alzain.dto.TestDTO;
import com.alzain.entity.TestItem;
import com.alzain.service.AdminTestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tests")
@CrossOrigin(origins = "*")
public class AdminTestController {

    @Autowired
    private AdminTestService adminTestService;

    @GetMapping
    public ResponseEntity<List<TestItem>> getAllTests() {
        return ResponseEntity.ok(adminTestService.getAllTests());
    }

    @PostMapping
    public ResponseEntity<TestItem> createTest(@Valid @RequestBody TestDTO dto) {
        TestItem created = adminTestService.createTest(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestItem> updateTest(@PathVariable Long id, @Valid @RequestBody TestDTO dto) {
        TestItem updated = adminTestService.updateTest(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTest(@PathVariable Long id) {
        adminTestService.deleteTest(id);
        return ResponseEntity.noContent().build();
    }
}
