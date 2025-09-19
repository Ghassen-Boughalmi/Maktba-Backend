package com.tn.maktba.dto.auth;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MFAData {
    private final String code;
    private final LocalDateTime expirationTime;
}
