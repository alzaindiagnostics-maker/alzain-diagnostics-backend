package com.alzain.service;

import com.alzain.dto.AdminAuthResponseDTO;
import com.alzain.dto.AdminLoginDTO;
import com.alzain.dto.ResetPasswordDTO;
import com.alzain.entity.PasswordResetToken;
import com.alzain.entity.User;
import com.alzain.exception.BadRequestException;
import com.alzain.repository.PasswordResetTokenRepository;
import com.alzain.repository.UserRepository;
import com.alzain.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AdminAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Value("${app.admin.frontend.url:http://localhost:5174}")
    private String adminFrontendUrl;

    @Transactional
    public AdminAuthResponseDTO loginAdmin(AdminLoginDTO dto) {
        String identifier = dto.getEmail() != null && !dto.getEmail().trim().isEmpty()
                ? dto.getEmail().trim()
                : (dto.getUsername() != null ? dto.getUsername().trim() : "");

        if (identifier.isEmpty() || dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new BadRequestException("Username/Email and password are required.");
        }

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new BadRequestException("Invalid admin credentials."));

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BadRequestException("Access denied. Account is not an administrator.");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid admin credentials.");
        }

        String token = jwtUtils.generateTokenFromUsername(user.getUsername());

        return AdminAuthResponseDTO.builder()
                .token(token)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public String processForgotPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Admin email is required.");
        }

        Optional<User> userOptional = userRepository.findByEmail(email.trim());

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                return "If the email is associated with an admin account, a password reset link has been sent.";
            }

            tokenRepository.deleteByUser(user);

            String rawToken = UUID.randomUUID().toString() + UUID.randomUUID().toString();
            String hashedToken = hashToken(rawToken);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .tokenHash(hashedToken)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(30))
                    .used(false)
                    .build();

            tokenRepository.save(resetToken);

            String resetLink = adminFrontendUrl.replaceAll("/+$", "") + "/reset-password?token=" + rawToken;
            log.info("Generated admin password reset link for [{}]: {}", user.getEmail(), resetLink);

            sendResetEmailOrLog(user.getEmail(), resetLink);
        }

        return "If the email is associated with an admin account, a password reset link has been sent.";
    }

    @Transactional
    public String resetPassword(ResetPasswordDTO dto) {
        if (dto.getToken() == null || dto.getToken().trim().isEmpty()) {
            throw new BadRequestException("Password reset token is required.");
        }

        if (dto.getNewPassword() == null || dto.getNewPassword().trim().length() < 6) {
            throw new BadRequestException("New password must be at least 6 characters long.");
        }

        String hashedInputToken = hashToken(dto.getToken().trim());
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(hashedInputToken)
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token."));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Password reset token has expired or already been used.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(dto.getNewPassword().trim()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return "Password reset successfully.";
    }

    private void sendResetEmailOrLog(String recipientEmail, String resetLink) {
        try {
            emailNotificationService.sendPasswordResetNotification(recipientEmail, resetLink);
            log.info("Password reset email request handed to EmailNotificationService for recipient: {}", recipientEmail);
        } catch (Exception e) {
            log.warn("Password Reset Email Trace Warning: {}", e.getMessage());
        }
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
