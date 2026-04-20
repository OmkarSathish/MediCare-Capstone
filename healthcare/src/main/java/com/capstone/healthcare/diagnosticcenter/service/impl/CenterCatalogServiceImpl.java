package com.capstone.healthcare.diagnosticcenter.service.impl;

import com.capstone.healthcare.diagnosticcenter.model.CenterTestOffering;
import com.capstone.healthcare.diagnosticcenter.model.CenterTestOfferingKey;
import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnosticcenter.repository.ICenterTestOfferingRepository;
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
        private final ICenterTestOfferingRepository offeringRepository;

        @Override
        public CenterTestOffering viewTestOffering(int diagnosticCenterId, String testName) {
                return offeringRepository.findByCenter_IdAndTest_TestName(diagnosticCenterId, testName)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Test '" + testName + "' not offered at center id="
                                                                + diagnosticCenterId));
        }

        @Override
        @Transactional
        public CenterTestOffering addTest(int diagnosticCenterId, int testId, double price) {
                DiagnosticCenter center = centerRepository.findById(diagnosticCenterId)
                                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticCenter", "id",
                                                diagnosticCenterId));
                DiagnosticTest test = testRepository.findById(testId)
                                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticTest", "id", testId));
                CenterTestOffering offering = CenterTestOffering.builder()
                                .id(new CenterTestOfferingKey(diagnosticCenterId, testId))
                                .center(center)
                                .test(test)
                                .price(price)
                                .build();
                return offeringRepository.save(offering);
        }
}
