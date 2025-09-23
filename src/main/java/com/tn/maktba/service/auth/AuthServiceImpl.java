package com.tn.maktba.service.auth;

import com.tn.maktba.dto.auth.*;
import com.tn.maktba.dto.user.UserEntityDTOMapper;
import com.tn.maktba.exceptions.InvalidTokenException;
import com.tn.maktba.exceptions.ResourceNotFoundException;
import com.tn.maktba.model.token.RefreshToken;
import com.tn.maktba.model.token.Token;
import com.tn.maktba.model.token.TokenType;
import com.tn.maktba.model.user.UserEntity;
import com.tn.maktba.repository.TokenRepository;
import com.tn.maktba.repository.UserRepository;
import com.tn.maktba.security.jwt.JwtService;
import com.tn.maktba.service.sms.VerificationCodeService;
import com.tn.maktba.service.token.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserEntityDTOMapper userEntityDTOMapper;
    private final AuthenticationManager authenticationManager;
    private final VerificationCodeService verificationCodeService;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(UserRepository userRepository, TokenRepository tokenRepository,
                           JwtService jwtService, PasswordEncoder passwordEncoder,
                           UserEntityDTOMapper userEntityDTOMapper, AuthenticationManager authenticationManager,
                           VerificationCodeService verificationCodeService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userEntityDTOMapper = userEntityDTOMapper;
        this.authenticationManager = authenticationManager;
        this.verificationCodeService = verificationCodeService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public ResponseEntity<?> register(RegisterRequest request) {
        if (userRepository.existsByIdCartNumber(request.getIdCartNumber())) {
            throw new IllegalArgumentException("ID Cart Number already in use: " + request.getIdCartNumber());
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already in use: " + request.getPhoneNumber());
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
        String verificationCode = verificationCodeService.generateVerificationCode(savedUser);
        String accessToken = jwtService.generateJwtToken(savedUser);
        String refreshToken = refreshTokenService.generateRefreshToken(savedUser);
        saveUserToken(savedUser, accessToken);
        AuthResponse response = AuthResponse.builder()
                .userEntityDTO(userEntityDTOMapper.apply(savedUser))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> login(AuthRequest request) {
        if ((request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) &&
                (request.getCin() == null || request.getCin().isEmpty())) {
            throw new IllegalArgumentException("Phone number or CIN must be provided");
        }

        UserEntity user = null;
        
        if (request.getCin() != null && !request.getCin().isEmpty()) {
            user = userRepository.findByIdCartNumber(request.getCin())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with CIN: " + request.getCin()));
        } else if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with phone number: " + request.getPhoneNumber()));
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getIdCartNumber(), request.getPassword()));

        revokeAllUserTokens(user);
        String jwtToken = jwtService.generateJwtToken(user);
        String refreshToken = refreshTokenService.generateRefreshToken(user);
        saveUserToken(user, jwtToken);

        AuthResponse logInResponse = AuthResponse.builder()
                .userEntityDTO(userEntityDTOMapper.apply(user))
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();

        return ResponseEntity.ok(logInResponse);
    }

    @Override
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Missing or invalid Authorization header");
        }

        String refreshToken = authHeader.substring(7);
        refreshTokenService.validateRefreshToken(refreshToken);
        String idCartNumber = jwtService.getIdCartNumberFromJwtToken(refreshToken);
        if (idCartNumber == null) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        UserEntity user = userRepository.findByIdCartNumber(idCartNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with CIN: " + idCartNumber));

        revokeAllUserTokens(user);
        String newAccessToken = jwtService.generateJwtToken(user);
        String newRefreshToken = refreshTokenService.generateRefreshToken(user);
        saveUserToken(user, newAccessToken);

        AuthResponse authResponse = AuthResponse.builder()
                .userEntityDTO(userEntityDTOMapper.apply(user))
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
        return ResponseEntity.ok(authResponse);
    }

    @Override
    public ResponseEntity<?> changePassword(ChangePasswordRequest request) {
        if ((request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) &&
                (request.getIdCartNumber() == null || request.getIdCartNumber().isEmpty())) {
            throw new IllegalArgumentException("Phone number or ID Cart Number must be provided");
        }

        UserEntity user = null;
        if (request.getIdCartNumber() != null && !request.getIdCartNumber().isEmpty()) {
            user = userRepository.findByIdCartNumber(request.getIdCartNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with CIN: " + request.getIdCartNumber()));
        } else if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with phone number: " + request.getPhoneNumber()));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        revokeAllUserTokens(user);
        String newAccessToken = jwtService.generateJwtToken(user);
        String newRefreshToken = refreshTokenService.generateRefreshToken(user);
        saveUserToken(user, newAccessToken);

        AuthResponse response = AuthResponse.builder()
                .userEntityDTO(userEntityDTOMapper.apply(user))
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> sendVerificationCode(String phoneNumber) {
        UserEntity user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone number: " + phoneNumber));
        String code = verificationCodeService.generateVerificationCode(user);
        return ResponseEntity.ok("Verification code sent successfully.");
    }

    @Override
    public ResponseEntity<?> verifyCode(String phoneNumber, String code) {
        verificationCodeService.validateVerificationCode(phoneNumber, code);
        return ResponseEntity.ok("Verification successful.");
    }

    private void saveUserToken(UserEntity user, String jwtToken) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.ACCESS)
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
        List<RefreshToken> validRefreshTokens = refreshTokenService.fetchAllRefreshTokenByUserId(user.getId());
        if (!validRefreshTokens.isEmpty()) {
            validRefreshTokens.forEach(token -> {
                token.setExpired(true);
                token.setRevoked(true);
            });
            refreshTokenService.saveAll(validRefreshTokens);
        }
    }
}
