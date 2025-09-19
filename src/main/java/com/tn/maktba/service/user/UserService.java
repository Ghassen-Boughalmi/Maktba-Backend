package com.tn.maktba.service.user;

import com.tn.maktba.dto.user.UpdateUserRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    ResponseEntity<?> updateUser(Long userId, UpdateUserRequest request);
    ResponseEntity<?> getAllUsers();
    ResponseEntity<?> getUserById(Long userId);
}
