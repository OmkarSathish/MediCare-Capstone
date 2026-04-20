package com.capstone.healthcare.diagnosticcenter.service.impl;

import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnosticcenter.repository.IDiagnosticCenterRepository;
import com.capstone.healthcare.diagnosticcenter.service.ICenterLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CenterLookupServiceImpl implements ICenterLookupService {

    private final IDiagnosticCenterRepository centerRepository;

    @Override
    public List<DiagnosticCenter> searchCenters(String keyword) {
        return centerRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public List<DiagnosticCenter> getCentersOfferingTest(int testId) {
        return centerRepository.findByTestId(testId);
    }
}
