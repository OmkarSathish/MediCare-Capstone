package com.capstone.healthcare.patient.service;

import com.capstone.healthcare.patient.model.Patient;
import com.capstone.healthcare.patient.model.TestResult;

import java.util.Set;

public interface ITestResultService {

    TestResult addTestResult(TestResult tr);

    TestResult updateResult(TestResult tr);

    TestResult removeTestResult(int id);

    Set<TestResult> viewResultsByPatient(Patient patient);
}
