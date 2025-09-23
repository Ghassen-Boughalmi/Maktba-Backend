package com.tn.maktba.security.jwt;

import com.tn.maktba.exceptions.ExpiredTokenException;
import com.tn.maktba.exceptions.InvalidTokenException;
import com.tn.maktba.exceptions.RevokedTokenException;
import com.tn.maktba.model.token.TokenType;
import com.tn.maktba.model.user.UserEntity;
import com.tn.maktba.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        String idCartNumber = jwtService.getIdCartNumberFromJwtToken(jwt);
        if (idCartNumber == null) {
            throw new InvalidTokenException("Invalid token: Unable to extract idCartNumber.");
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UserEntity userDetails = (UserEntity) userDetailsService.loadUserByUsername(idCartNumber);
            boolean isTokenValid = tokenRepository.findByToken(jwt)
                    .map(t -> {
                        if (t.isExpired()) {
                            throw new ExpiredTokenException("Token has expired.");
                        }
                        if (t.isRevoked()) {
                            throw new RevokedTokenException("Token has been revoked.");
                        }
                        if (t.getTokenType() != TokenType.ACCESS) {
                            throw new InvalidTokenException("Token type is not access.");
                        }
                        return true;
                    })
                    .orElseThrow(() -> new InvalidTokenException("Token not found in repository."));

            if (!jwtService.isTokenValid(jwt, userDetails)) {
                throw new InvalidTokenException("Token validation failed.");
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}