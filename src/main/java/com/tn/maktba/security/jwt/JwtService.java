package com.tn.maktba.security.jwt;

import com.tn.maktba.exceptions.InvalidTokenException;
import com.tn.maktba.model.user.UserEntity;
import com.tn.maktba.security.utility.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private final Key accessKey;

    public JwtService() {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SecurityConstants.JWT_ACCES_SECRET_KEY));
    }

    public String getIdCartNumberFromJwtToken(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtException e) {
            logger.error("Failed to extract idCartNumber from token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token");
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            return extractClaim(token, claimsResolver, accessKey);
        } catch (JwtException e) {
            logger.error("Failed to extract claim from token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token");
        }
    }

    public String generateJwtToken(UserEntity userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("phoneNumber", userDetails.getPhoneNumber());
        claims.put("type", "access");
        return buildToken(claims, userDetails, SecurityConstants.JWT_ACCES_EXPIRATION, accessKey);
    }

    private String buildToken(Map<String, Object> claims, UserEntity userDetails, long expirationTime, Key key) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getIdCartNumber())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserEntity userDetails) {
        try {
            String idCartNumber = extractClaim(token, Claims::getSubject);
            String type = extractClaim(token, claims -> claims.get("type", String.class));
            if (!"access".equals(type)) {
                throw new InvalidTokenException("Token type is not access.");
            }
            return idCartNumber != null && idCartNumber.equals(userDetails.getIdCartNumber()) && !isTokenExpired(token);
        } catch (JwtException e) {
            logger.error("Token validation failed: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token");
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractClaim(token, Claims::getExpiration, accessKey).before(new Date());
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid token");
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver, Key key) {
        Claims claims = extractAllClaims(token, key);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, Key key) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}