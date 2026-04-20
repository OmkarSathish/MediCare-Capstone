package com.capstone.healthcare.patient.repository;

import com.capstone.healthcare.patient.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPatientRepository extends JpaRepository<Patient, Integer> {

    /**
     * viewPatient — looks up a patient by their auth username.
     */
    Optional<Patient> findByUsername(String username);

    boolean existsByUsername(String username);
}
