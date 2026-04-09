package com.capstone.healthcare.patient.service.impl;

import com.capstone.healthcare.patient.model.Patient;
import com.capstone.healthcare.patient.model.TestResult;
import com.capstone.healthcare.patient.repository.IPatientRepository;
import com.capstone.healthcare.patient.repository.ITestResultRepository;
import com.capstone.healthcare.patient.service.IPatientService;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import com.capstone.healthcare.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements IPatientService {

    private final IPatientRepository patientRepository;
    private final ITestResultRepository testResultRepository;

    @Override
    @Transactional
    public Patient registerPatient(Patient patient) {
        if (patientRepository.existsByUsername(patient.getUsername())) {
            throw new ValidationException("Patient already registered: " + patient.getUsername());
        }
        return patientRepository.save(patient);
    }

    @Override
    @Transactional
    public Patient updatePatientDetails(Patient patient) {
        patientRepository.findById(patient.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patient.getPatientId()));
        return patientRepository.save(patient);
    }

    @Override
    public Patient viewPatient(String patientUserName) {
        return patientRepository.findByUsername(patientUserName)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "username", patientUserName));
    }

    @Override
    public Set<TestResult> getAllTestResult(String patientUserName) {
        viewPatient(patientUserName);
        return new HashSet<>(testResultRepository.findByPatientUsername(patientUserName));
    }

    @Override
    public TestResult viewTestResult(int testResultId) {
        return testResultRepository.findById(testResultId)
                .orElseThrow(() -> new ResourceNotFoundException("TestResult", "id", testResultId));
    }
}
