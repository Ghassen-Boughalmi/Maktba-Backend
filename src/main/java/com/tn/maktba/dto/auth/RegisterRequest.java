package com.tn.maktba.dto.auth;



import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "FirstName is required")
    private String firstName;

    @NotBlank(message = "LastName is required")
    private String lastName;

    @NotBlank(message = "PhoneNumber is required")
    private String phoneNumber;

    @NotBlank(message = "IdCartNumber is required")
    private String idCartNumber;

    @NotBlank(message = "Password is required")
    private String password;


    private String email;


    private String address;
}
