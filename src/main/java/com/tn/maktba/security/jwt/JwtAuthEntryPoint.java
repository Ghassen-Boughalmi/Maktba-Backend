package com.tn.maktba.security.jwt;

import com.tn.maktba.exceptions.ApiError;
import com.tn.maktba.exceptions.ResponseEntityBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws java.io.IOException {
        List<String> details = new ArrayList<>();
        details.add(authException.getMessage());

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(org.springframework.http.HttpStatus.UNAUTHORIZED.value())
                .message("Authentication failed.")
                .errors(details)
                .build();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        new com.fasterxml.jackson.databind.ObjectMapper().writeValue(response.getOutputStream(), ResponseEntityBuilder.build(apiError));
    }
}