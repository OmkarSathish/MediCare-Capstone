package com.capstone.healthcare.diagnostictest.service;

import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;

import java.util.List;

public interface IDiagnosticTestService {

    List<DiagnosticTest> getAllTest();

    DiagnosticTest addNewTest(DiagnosticTest test);

    List<DiagnosticTest> getTestsOfDiagnosticCenter(int centerId);

    DiagnosticTest updateTestDetail(DiagnosticTest test);

    DiagnosticTest removeTestFromDiagnosticCenter(int centerId, DiagnosticTest test);
}
