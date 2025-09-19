package com.tn.maktba.service.user;

import com.tn.maktba.dto.user.UserEntityDTO;
import com.tn.maktba.dto.user.UpdateUserRequest;
import com.tn.maktba.model.user.UserEntity;
import com.tn.maktba.repository.UserRepository;
import com.tn.maktba.dto.user.UserEntityDTOMapper;
import com.tn.maktba.service.auth.AuthService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserEntityDTOMapper userEntityDTOMapper;
    private final AuthService authService;

    @Override
    @Transactional
    public ResponseEntity<?> updateUser(Long userId, UpdateUserRequest request) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
                user.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null && !request.getLastName().isEmpty()) {
                user.setLastName(request.getLastName());
            }
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                if (userRepository.findByEmail(request.getEmail()).isPresent() &&
                        !user.getEmail().equals(request.getEmail())) {
                    throw new IllegalArgumentException("Email already in use");
                }
                user.setEmail(request.getEmail());
            }
            if (request.getAddress() != null && !request.getAddress().isEmpty()) {
                user.setAddress(request.getAddress());
            }
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty() &&
                    !user.getPhoneNumber().equals(request.getPhoneNumber())) {
                if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
                    throw new IllegalArgumentException("Phone number already in use");
                }
                if (request.getVerificationCode() == null || request.getVerificationCode().isEmpty()) {
                    throw new IllegalArgumentException("Verification code required for phone number change");
                }
                ResponseEntity<?> verificationResponse = authService.verifyCode(request.getPhoneNumber(), request.getVerificationCode());
                if (verificationResponse.getStatusCodeValue() != 200) {
                    return verificationResponse;
                }
                user.setPhoneNumber(request.getPhoneNumber());
            }

            userRepository.save(user);
            return ResponseEntity.ok(userEntityDTOMapper.apply(user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update user: " + e.getMessage()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserEntityDTO> users = userRepository.findAll().stream()
                    .map(userEntityDTOMapper)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve users: " + e.getMessage()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getUserById(Long userId) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            return ResponseEntity.ok(userEntityDTOMapper.apply(user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve user: " + e.getMessage()));
        }
    }
}