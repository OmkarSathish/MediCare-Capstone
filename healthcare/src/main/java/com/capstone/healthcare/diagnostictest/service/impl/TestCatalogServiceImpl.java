package com.capstone.healthcare.diagnostictest.service.impl;

import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;
import com.capstone.healthcare.diagnostictest.repository.IDiagnosticTestRepository;
import com.capstone.healthcare.diagnostictest.service.ITestCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestCatalogServiceImpl implements ITestCatalogService {

    private final IDiagnosticTestRepository testRepository;

    @Override
    public List<DiagnosticTest> searchTests(String keyword) {
        return testRepository.findByNameOrCategoryName(keyword);
    }

    @Override
    public List<DiagnosticTest> getTestsByCategory(int categoryId) {
        return testRepository.findByCategory_CategoryId(categoryId);
    }
}
