package com.capstone.healthcare.patient.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientProfileRequest {

    @NotBlank
    private String name;

    private String phoneNo;

    @Min(0)
    private int age;

    private String gender;
}
