package com.tn.maktba.service.auth;

import com.tn.maktba.dto.auth.*;
import com.tn.maktba.dto.user.UserEntityDTOMapper;
import com.tn.maktba.model.token.Token;
import com.tn.maktba.model.token.TokenType;
import com.tn.maktba.model.user.UserEntity;
import com.tn.maktba.repository.TokenRepository;
import com.tn.maktba.repository.UserRepository;
import com.tn.maktba.security.jwt.JwtService;
import com.tn.maktba.service.sms.SmsService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserEntityDTOMapper userEntityDTOMapper;
    private final AuthenticationManager authenticationManager;
    private final SmsService smsService;

    private final Map<String, MFAData> verificationCodes = new ConcurrentHashMap<>();

    @Override
    public ResponseEntity<?> register(RegisterRequest request) {
        try {
            if (userRepository.existsByIdCartNumber(request.getIdCartNumber())) {
                throw new AuthException("ID Cart Number already in use: " + request.getIdCartNumber());
            }
            UserEntity user = UserEntity.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phoneNumber(request.getPhoneNumber())
                    .idCartNumber(request.getIdCartNumber())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email(request.getEmail())
                    .address(request.getAddress())
                    .build();
            UserEntity savedUser = userRepository.save(user);
            String accessToken = jwtService.generateJwtToken(savedUser);
            String refreshToken = jwtService.generateJwtRefreshToken(savedUser);
            saveUserToken(savedUser, accessToken);
            saveUserToken(savedUser, refreshToken);
            AuthResponse response = AuthResponse.builder()
                    .userEntityDTO(userEntityDTOMapper.apply(savedUser))
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
            return ResponseEntity.ok(response);
        } catch (AuthException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> login(AuthRequest request) {
        try {
            if ((request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) &&
                    (request.getCin() == null || request.getCin().isEmpty())) {
                throw new AuthException("Phone number or CIN must be provided");
            }

            UserEntity user = null;
            String identifier = null;

            if (request.getCin() != null && !request.getCin().isEmpty()) {
                user = userRepository.findByIdCartNumber(request.getCin()).orElse(null);
                identifier = request.getCin();
            } else if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
                user = userRepository.findByPhoneNumber(request.getPhoneNumber()).orElse(null);
                identifier = request.getPhoneNumber();
            }

            if (user == null) {
                throw new AuthException("User not found with provided credentials");
            }

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(identifier, request.getPassword()));

            String jwtToken = jwtService.generateJwtToken(user);
            String refreshToken = jwtService.generateJwtRefreshToken(user);
            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);
            saveUserToken(user, refreshToken);

            AuthResponse logInResponse = AuthResponse.builder()
                    .userEntityDTO(userEntityDTOMapper.apply(user))
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();

            return ResponseEntity.ok(logInResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid password"));
        } catch (AuthException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication failed"));
        }
    }

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String refreshToken = authHeader.substring(7);
        String tokenType = jwtService.extractClaim(refreshToken, claims -> claims.get("type", String.class));
        if (!"refresh".equals(tokenType)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token type");
            return;
        }

        String idCartNumber = jwtService.getIdCartNumberFromJwtToken(refreshToken);
        if (idCartNumber == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token");
            return;
        }

        UserEntity user = userRepository.findByIdCartNumber(idCartNumber).orElse(null);
        if (user == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        }

        boolean isTokenValid = tokenRepository.findByToken(refreshToken)
                .map(t -> !t.isExpired() && !t.isRevoked())
                .orElse(false);

        if (jwtService.isTokenValid(refreshToken, user) && isTokenValid) {
            String newAccessToken = jwtService.generateJwtToken(user);
            saveUserToken(user, newAccessToken);
            AuthResponse authResponse = AuthResponse.builder()
                    .userEntityDTO(userEntityDTOMapper.apply(user))
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .build();
            response.setContentType("application/json");
            response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(authResponse));
        } else {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired refresh token");
        }
    }

    @Override
    public ResponseEntity<?> changePassword(ChangePasswordRequest request) {
        try {
            if ((request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) &&
                    (request.getIdCartNumber() == null || request.getIdCartNumber().isEmpty())) {
                throw new AuthException("Phone number or ID Cart Number must be provided");
            }

            UserEntity user = null;
            if (request.getIdCartNumber() != null && !request.getIdCartNumber().isEmpty()) {
                user = userRepository.findByIdCartNumber(request.getIdCartNumber())
                        .orElse(null);
            } else if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
                user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                        .orElse(null);
            }

            if (user == null) {
                throw new AuthException("User not found with provided credentials");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            revokeAllUserTokens(user);

            String newAccessToken = jwtService.generateJwtToken(user);
            String newRefreshToken = jwtService.generateJwtRefreshToken(user);
            saveUserToken(user, newAccessToken);
            saveUserToken(user, newRefreshToken);

            AuthResponse response = AuthResponse.builder()
                    .userEntityDTO(userEntityDTOMapper.apply(user))
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();
            return ResponseEntity.ok(response);
        } catch (AuthException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Password change failed"));
        }
    }

    @Override
    public ResponseEntity<?> sendVerificationCode(String phoneNumber) {
        try {
            String code = String.format("%06d", new Random().nextInt(999999));
            verificationCodes.put(phoneNumber, new MFAData(code, LocalDateTime.now().plusMinutes(5)));

            String message = String.format("Your verification code is: %s", code);
            smsService.sendSms(phoneNumber, message);

            return ResponseEntity.ok("Verification code sent successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send verification code"));
        }
    }

    @Override
    public ResponseEntity<?> verifyCode(String phoneNumber, String code) {
        try {
            MFAData storedData = verificationCodes.get(phoneNumber);
            if (storedData == null) {
                verificationCodes.remove(phoneNumber);
                return ResponseEntity.status(401).body(Map.of("error", "Invalid verification code"));
            }

            if (LocalDateTime.now().isAfter(storedData.getExpirationTime())) {
                verificationCodes.remove(phoneNumber);
                return ResponseEntity.status(401).body(Map.of("error", "Verification code is expired"));
            }

            if (!storedData.getCode().equals(code)) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid verification code"));
            }

            verificationCodes.remove(phoneNumber);
            return ResponseEntity.ok("Verification successful.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Verification failed"));
        }
    }

    private void saveUserToken(UserEntity user, String jwtToken) {
        String tokenTypeClaim = jwtService.extractClaim(jwtToken, claims -> claims.get("type", String.class));
        TokenType tokenType = "refresh".equals(tokenTypeClaim) ? TokenType.REFRESH : TokenType.ACCESS;

        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(tokenType)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(UserEntity user) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach(token -> {
                token.setExpired(true);
                token.setRevoked(true);
            });
            tokenRepository.saveAll(validUserTokens);
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(Map.of("error", message)));
    }
}