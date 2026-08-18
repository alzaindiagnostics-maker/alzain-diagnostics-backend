package com.alzain.controller;

import com.alzain.dto.AdminAuthResponseDTO;
import com.alzain.dto.AdminLoginDTO;
import com.alzain.dto.ForgotPasswordRequestDTO;
import com.alzain.dto.ResetPasswordDTO;
import com.alzain.service.AdminAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    @PostMapping("/admin/login")
    public ResponseEntity<AdminAuthResponseDTO> adminLogin(@Valid @RequestBody AdminLoginDTO loginRequest) {
        AdminAuthResponseDTO response = adminAuthService.loginAdmin(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AdminAuthResponseDTO> legacyLogin(@Valid @RequestBody AdminLoginDTO loginRequest) {
        AdminAuthResponseDTO response = adminAuthService.loginAdmin(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        String message = adminAuthService.processForgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/admin/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDTO request) {
        String message = adminAuthService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
