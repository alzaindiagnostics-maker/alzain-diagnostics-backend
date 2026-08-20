package com.alzain.service;

import com.alzain.dto.AdminAuthResponseDTO;
import com.alzain.dto.AdminLoginDTO;
import com.alzain.entity.User;
import com.alzain.exception.BadRequestException;
import com.alzain.repository.UserRepository;
import com.alzain.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AdminAuthResponseDTO loginAdmin(AdminLoginDTO loginRequest) {
        String usernameInput = loginRequest.getUsername() != null ? loginRequest.getUsername().trim() : "";
        String emailInput = loginRequest.getEmail() != null ? loginRequest.getEmail().trim() : "";
        String identifier = !usernameInput.isEmpty() ? usernameInput : emailInput;

        // 1. Fetch user by username or email
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        // 2. Validate BCrypt hashed password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid username or password");
        }

        // 3. Validate Admin Privilege
        String role = user.getRole() != null ? user.getRole().trim() : "";
        if (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("ROLE_ADMIN")) {
            throw new BadRequestException("Access denied. Admin privileges required.");
        }

        // 4. Generate JWT
        String token = jwtUtils.generateTokenFromUsername(user.getUsername());

        // Standardize role string for frontend compatibility ("ADMIN")
        String cleanRole = role.toUpperCase().replace("ROLE_", "");

        log.info("Admin login successful for user: {}, role: {}", user.getUsername(), cleanRole);

        return AdminAuthResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(cleanRole) // Returns "ADMIN"
                .build();
    }
}