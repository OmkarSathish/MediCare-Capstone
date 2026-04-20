package com.capstone.healthcare.patient.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(schema = "patient_schema", name = "patients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private int patientId;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_no")
    private String phoneNo;

    private int age;

    private String gender;

    @Column(nullable = false, unique = true)
    private String username;
}
