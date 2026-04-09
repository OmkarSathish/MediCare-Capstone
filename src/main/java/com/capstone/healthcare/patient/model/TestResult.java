package com.capstone.healthcare.patient.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "appointment_id", nullable = false)
    private int appointmentId;

    @Column(name = "test_reading", nullable = false)
    private String testReading;

    @Column(nullable = false)
    private String medicalCondition;
}
