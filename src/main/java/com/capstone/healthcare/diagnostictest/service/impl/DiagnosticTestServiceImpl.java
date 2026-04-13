package com.capstone.healthcare.diagnostictest.service.impl;

import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnosticcenter.repository.IDiagnosticCenterRepository;
import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;
import com.capstone.healthcare.diagnostictest.repository.IDiagnosticTestRepository;
import com.capstone.healthcare.diagnostictest.service.IDiagnosticTestService;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosticTestServiceImpl implements IDiagnosticTestService {

    private final IDiagnosticTestRepository testRepository;
    private final IDiagnosticCenterRepository centerRepository;

    @Override
    public List<DiagnosticTest> getAllTest() {
        return testRepository.findAll();
    }

    @Override
    @Transactional
    public DiagnosticTest addNewTest(DiagnosticTest test) {
        test.setStatus("ACTIVE");
        return testRepository.save(test);
    }

    @Override
    public List<DiagnosticTest> getTestsOfDiagnosticCenter(int centerId) {
        return testRepository.findByCenterId(centerId);
    }

    @Override
    @Transactional
    public DiagnosticTest updateTestDetail(DiagnosticTest test) {
        DiagnosticTest existing = testRepository.findById(test.getId())
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticTest", "id", test.getId()));
        existing.setTestName(test.getTestName());
        existing.setTestPrice(test.getTestPrice());
        return testRepository.save(existing);
    }

    @Override
    @Transactional
    public DiagnosticTest removeTestFromDiagnosticCenter(int centerId, DiagnosticTest test) {
        DiagnosticCenter center = centerRepository.findById(centerId)
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticCenter", "id", centerId));
        center.getTestOfferings().removeIf(o -> o.getTest().getId() == test.getId());
        centerRepository.save(center);
        return test;
    }
}
