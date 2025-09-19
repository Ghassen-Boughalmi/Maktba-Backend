package com.tn.maktba.dto.user;

import java.util.Set;

public record UserEntityDTO(
        Long id,
        String firstName,
        String lastName,
        String phoneNumber,
        String idCartNumber,
        String email,
        String address

) {}