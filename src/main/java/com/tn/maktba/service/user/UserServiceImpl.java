package com.tn.maktba.service.user;

import com.tn.maktba.dto.user.UserEntityDTO;
import com.tn.maktba.dto.user.UpdateUserRequest;
import com.tn.maktba.model.user.UserEntity;
import com.tn.maktba.repository.UserRepository;
import com.tn.maktba.dto.user.UserEntityDTOMapper;
import com.tn.maktba.service.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserEntityDTOMapper userEntityDTOMapper;
    private final AuthService authService;

    public UserServiceImpl(UserRepository userRepository, UserEntityDTOMapper userEntityDTOMapper, AuthService authService) {
        this.userRepository = userRepository;
        this.userEntityDTOMapper = userEntityDTOMapper;
        this.authService = authService;
    }

    @Override
    public ResponseEntity<?> updateUser(Long userId, UpdateUserRequest request) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isEmpty()) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userRepository.findByEmail(request.getEmail()).isPresent() &&
                    !user.getEmail().equals(request.getEmail())) {
                return ResponseEntity.status(400).body(Map.of("error", "Email already in use"));
            }
            user.setEmail(request.getEmail());
        }
        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            user.setAddress(request.getAddress());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty() &&
                !user.getPhoneNumber().equals(request.getPhoneNumber())) {
            if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
                return ResponseEntity.status(400).body(Map.of("error", "Phone number already in use"));
            }
            if (request.getVerificationCode() == null || request.getVerificationCode().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "Verification code required for phone number change"));
            }
            ResponseEntity<?> verificationResponse = authService.verifyCode(request.getPhoneNumber(), request.getVerificationCode());
            if (verificationResponse.getStatusCodeValue() != 200) {
                return verificationResponse;
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }

        userRepository.save(user);
        return ResponseEntity.ok(userEntityDTOMapper.apply(user));
    }

    @Override
    public ResponseEntity<?> getAllUsers() {
        List<UserEntityDTO> users = userRepository.findAll().stream()
                .map(userEntityDTOMapper)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Override
    public ResponseEntity<?> getUserById(Long userId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
        return ResponseEntity.ok(userEntityDTOMapper.apply(user));
    }
}