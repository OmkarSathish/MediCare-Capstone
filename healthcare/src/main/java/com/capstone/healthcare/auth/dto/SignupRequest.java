package com.capstone.healthcare.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @Pattern(regexp = "^(\\+91[\\-\\s]?|0)?[6-9]\\d{9}$|^$", message = "Enter a valid 10-digit Indian mobile number (e.g. 98765 43210 or +91 98765 43210)")
    private String phone;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /** Expected values: CUSTOMER or ADMIN */
    @NotBlank
    private String role;
}
