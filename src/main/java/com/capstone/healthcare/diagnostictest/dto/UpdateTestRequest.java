package com.capstone.healthcare.diagnostictest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTestRequest {

    private int id;

    @NotBlank
    private String testName;

    @Positive
    private double testPrice;

    private String normalValue;

    private String units;
}
