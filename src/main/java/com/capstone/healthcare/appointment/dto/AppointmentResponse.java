package com.capstone.healthcare.appointment.dto;

import com.capstone.healthcare.appointment.model.ApprovalStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {

    private int id;
    private LocalDate appointmentDate;
    private ApprovalStatus approvalStatus;
    private String patientName;
    private String centerName;
    private String remarks;
    private String specialRequests;
}
