package com.capstone.healthcare.patient.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientProfileResponse {

    private int patientId;
    private String name;
    private String phoneNo;
    private int age;
    private String gender;
    private String username;
}
