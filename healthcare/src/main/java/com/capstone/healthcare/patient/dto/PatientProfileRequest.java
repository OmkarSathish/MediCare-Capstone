package com.capstone.healthcare.patient.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientProfileRequest {

    @NotBlank
    private String name;

    @Pattern(regexp = "^(\\+91[\\-\\s]?|0)?[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number (e.g. 98765 43210 or +91 98765 43210)")
    private String phoneNo;

    @Min(0)
    private int age;

    private String gender;
}
