package com.capstone.healthcare.patient.service.impl;

import com.capstone.healthcare.patient.model.Patient;
import com.capstone.healthcare.patient.model.TestResult;
import com.capstone.healthcare.patient.repository.ITestResultRepository;
import com.capstone.healthcare.patient.service.ITestResultService;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TestResultServiceImpl implements ITestResultService {

    private final ITestResultRepository testResultRepository;

    @Override
    @Transactional
    public TestResult addTestResult(TestResult tr) {
        return testResultRepository.save(tr);
    }

    @Override
    @Transactional
    public TestResult updateResult(TestResult tr) {
        testResultRepository.findById(tr.getId())
                .orElseThrow(() -> new ResourceNotFoundException("TestResult", "id", tr.getId()));
        return testResultRepository.save(tr);
    }

    @Override
    @Transactional
    public TestResult removeTestResult(int id) {
        TestResult tr = testResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestResult", "id", id));
        testResultRepository.delete(tr);
        return tr;
    }

    @Override
    public Set<TestResult> viewResultsByPatient(Patient patient) {
        return new HashSet<>(testResultRepository.findByPatientUsername(patient.getUsername()));
    }
}
