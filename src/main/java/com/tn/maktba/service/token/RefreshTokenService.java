package com.tn.maktba.service.token;

import com.tn.maktba.model.token.RefreshToken;
import com.tn.maktba.model.user.UserEntity;

import java.util.List;
import java.util.UUID;

public interface RefreshTokenService {
    String generateRefreshToken(UserEntity user);
    RefreshToken fetchRefreshTokenByToken(String refreshToken);
    List<RefreshToken> fetchAllRefreshTokenByUserId(Long userId);
    void validateRefreshToken(String refreshToken);
    void saveAll(List<RefreshToken> refreshTokens);
}