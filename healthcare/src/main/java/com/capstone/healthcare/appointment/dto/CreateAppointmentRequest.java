package com.capstone.healthcare.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAppointmentRequest {

    @Positive
    private int centerId;

    @NotNull
    private Set<Integer> testIds;

    @NotNull
    @Future
    private LocalDate appointmentDate;

    private String specialRequests;
}
