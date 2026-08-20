package com.alzain.controller;

import com.alzain.dto.AdminAuthResponseDTO;
import com.alzain.dto.AdminLoginDTO;
import com.alzain.dto.ForgotPasswordRequestDTO;
import com.alzain.dto.ResetPasswordDTO;
import com.alzain.service.AdminAuthService;
import com.alzain.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AdminAuthService adminAuthService;
    private final AdminUserService adminUserService;

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
        String message = adminUserService.processForgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/admin/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDTO request) {
        String message = adminUserService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", message));
    }
}