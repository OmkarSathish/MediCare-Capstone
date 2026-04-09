package com.capstone.healthcare.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectAppointmentRequest {

    @NotBlank(message = "Rejection reason is required")
    private String remarks;
}
