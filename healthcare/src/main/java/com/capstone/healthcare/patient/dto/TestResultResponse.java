package com.capstone.healthcare.patient.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResultResponse {

    private int id;
    private String testReading;
    private String medicalCondition;
    private int appointmentId;
}
