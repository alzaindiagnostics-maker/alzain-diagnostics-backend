package com.alzain.service;

import com.alzain.dto.ChangePasswordDTO;
import com.alzain.dto.ResetPasswordDTO;
import com.alzain.dto.ResetPasswordRequestDTO;
import com.alzain.entity.User;
import com.alzain.exception.BadRequestException;
import com.alzain.exception.ResourceNotFoundException;
import com.alzain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserService {

    private static final String DEFAULT_RECOVERY_KEY = "8374874335";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotificationService emailNotificationService;

    @Value("${app.admin.frontend.url:https://alzain-diagnostics-admin-beta.vercel.app}")
    private String adminFrontendUrl;

    @Transactional
    public String processForgotPassword(String email) {
        String trimmedEmail = email != null ? email.trim() : "";
        User user = userRepository.findByEmail(trimmedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found with email: " + trimmedEmail));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String cleanBaseUrl = (adminFrontendUrl != null && !adminFrontendUrl.trim().isEmpty())
                ? adminFrontendUrl.trim().replaceAll("/+$", "")
                : "https://alzain-diagnostics-admin-beta.vercel.app";

        String resetLink = cleanBaseUrl + "/reset-password?token=" + resetToken;
        log.info("Generated password reset token for admin {}: {}", user.getUsername(), resetToken);

        emailNotificationService.sendPasswordResetNotification(user.getEmail(), resetLink);

        return "Password reset link sent successfully to " + trimmedEmail;
    }

    @Transactional
    public String resetPassword(ResetPasswordDTO dto) {
        String token = dto.getToken() != null ? dto.getToken().trim() : "";
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token."));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Password reset token has expired. Please request a new reset link.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword().trim()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Successfully reset password for admin user: {}", user.getUsername());

        return "Password reset successfully. You can now log in with your new password.";
    }

    @Transactional
    public void changePassword(String username, ChangePasswordDTO dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword().trim()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void resetPasswordWithRecoveryKey(ResetPasswordRequestDTO dto) {
        if (!DEFAULT_RECOVERY_KEY.equals(dto.getRecoveryKey().trim())) {
            throw new BadRequestException("Invalid recovery security key. Password reset failed.");
        }

        User user = userRepository.findByUsername(dto.getUsername().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found with username: " + dto.getUsername()));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword().trim()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
