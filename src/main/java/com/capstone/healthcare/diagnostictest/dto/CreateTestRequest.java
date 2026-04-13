package com.capstone.healthcare.diagnostictest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTestRequest {

    @NotBlank
    private String testName;

    @Positive
    private double testPrice;

    private Integer categoryId;
}
