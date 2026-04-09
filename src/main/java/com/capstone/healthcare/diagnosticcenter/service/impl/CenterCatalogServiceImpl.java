package com.capstone.healthcare.diagnosticcenter.service.impl;

import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnosticcenter.repository.IDiagnosticCenterRepository;
import com.capstone.healthcare.diagnosticcenter.service.ICenterCatalogService;
import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;
import com.capstone.healthcare.diagnostictest.repository.IDiagnosticTestRepository;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CenterCatalogServiceImpl implements ICenterCatalogService {

    private final IDiagnosticCenterRepository centerRepository;
    private final IDiagnosticTestRepository testRepository;

    @Override
    public DiagnosticTest viewTestDetails(int diagnosticCenterId, String testName) {
        DiagnosticCenter center = centerRepository.findCenterWithTest(diagnosticCenterId, testName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Test '" + testName + "' not offered at center id=" + diagnosticCenterId));
        return center.getTests().stream()
                .filter(t -> t.getTestName().equals(testName))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticTest", "testName", testName));
    }

    @Override
    @Transactional
    public DiagnosticTest addTest(int diagnosticCenterId, int testId) {
        DiagnosticCenter center = centerRepository.findById(diagnosticCenterId)
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticCenter", "id", diagnosticCenterId));
        DiagnosticTest test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticTest", "id", testId));
        center.getTests().add(test);
        centerRepository.save(center);
        return test;
    }
}
