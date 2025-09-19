package com.tn.maktba.dto.auth;

import com.tn.maktba.dto.user.UserEntityDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private UserEntityDTO userEntityDTO;
    private String accessToken;
    private String refreshToken;

}