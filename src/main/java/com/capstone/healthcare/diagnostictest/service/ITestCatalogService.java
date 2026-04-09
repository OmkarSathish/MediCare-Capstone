package com.capstone.healthcare.diagnostictest.service;

import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;

import java.util.List;

public interface ITestCatalogService {

    List<DiagnosticTest> searchTests(String keyword);

    List<DiagnosticTest> getTestsByCategory(int categoryId);
}
