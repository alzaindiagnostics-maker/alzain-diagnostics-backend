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

import com.alzain.security.JwtUtils;
import com.alzain.security.TokenBlacklistService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AdminAuthService adminAuthService;
    private final AdminUserService adminUserService;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

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

    @PostMapping({"/admin/logout", "/logout"})
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7).trim();
            if (!jwt.isEmpty() && jwtUtils.validateJwtToken(jwt)) {
                java.util.Date exp = jwtUtils.getExpirationFromJwtToken(jwt);
                long expirationMs = exp != null ? exp.getTime() : System.currentTimeMillis() + 86400000L;
                tokenBlacklistService.blacklistToken(jwt, expirationMs);
            }
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}