package com.capstone.healthcare.patient.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResultRequest {

    @Positive
    private int appointmentId;

    private String testReading;

    private String medicalCondition;
}
