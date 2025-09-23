package com.tn.maktba.service.auth;

import com.tn.maktba.dto.auth.AuthRequest;
import com.tn.maktba.dto.auth.ChangePasswordRequest;
import com.tn.maktba.dto.auth.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface AuthService {
    ResponseEntity<?> register(@NotNull RegisterRequest request);
    ResponseEntity<?> login(AuthRequest request);
    ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
    ResponseEntity<?> changePassword(@NotNull ChangePasswordRequest request);
    ResponseEntity<?> sendVerificationCode(String phoneNumber);
    ResponseEntity<?> verifyCode(String phoneNumber, String code);
}