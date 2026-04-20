package com.capstone.healthcare.diagnostictest.service.impl;

import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;
import com.capstone.healthcare.diagnostictest.repository.ITestRepository;
import com.capstone.healthcare.diagnostictest.service.ITestService;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements ITestService {

    private final ITestRepository testRepository;

    @Override
    @Transactional
    public DiagnosticTest addTest(DiagnosticTest test) {
        test.setStatus("ACTIVE");
        return testRepository.save(test);
    }

    @Override
    @Transactional
    public DiagnosticTest updateTest(DiagnosticTest test) {
        testRepository.findById(test.getId())
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticTest", "id", test.getId()));
        return testRepository.save(test);
    }

    @Override
    @Transactional
    public DiagnosticTest removeTest(DiagnosticTest test) {
        DiagnosticTest existing = testRepository.findById(test.getId())
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticTest", "id", test.getId()));
        existing.setStatus("INACTIVE");
        return testRepository.save(existing);
    }

    @Override
    public List<DiagnosticTest> viewAllTest(String criteria) {
        if (criteria == null || criteria.isBlank()) {
            return testRepository.findAll();
        }
        return testRepository.findByTestNameContainingIgnoreCase(criteria);
    }
}
