package com.capstone.healthcare.patient.service;

import com.capstone.healthcare.patient.model.Patient;
import com.capstone.healthcare.patient.model.TestResult;

import java.util.Set;

public interface IPatientService {

    Patient registerPatient(Patient patient);

    Patient updatePatientDetails(Patient patient);

    Patient viewPatient(String patientUserName);

    Set<TestResult> getAllTestResult(String patientUserName);

    TestResult viewTestResult(int testResultId);
}
