package com.alzain.service;

import com.alzain.dto.ChangePasswordDTO;
import com.alzain.dto.ResetPasswordRequestDTO;
import com.alzain.entity.User;
import com.alzain.exception.BadRequestException;
import com.alzain.exception.ResourceNotFoundException;
import com.alzain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminUserService {

    private static final String DEFAULT_RECOVERY_KEY = "8374874335";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public void resetPassword(ResetPasswordRequestDTO dto) {
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
