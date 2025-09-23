package com.tn.maktba.exceptions;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ApiError {
    private int status;
    private LocalDateTime timestamp;
    private String message;
    private List<String> errors;
}