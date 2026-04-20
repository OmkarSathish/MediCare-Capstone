package com.capstone.healthcare.diagnostictest.service;

import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;

import java.util.List;

public interface ITestService {

    DiagnosticTest addTest(DiagnosticTest test);

    DiagnosticTest updateTest(DiagnosticTest test);

    DiagnosticTest removeTest(DiagnosticTest test);

    List<DiagnosticTest> viewAllTest(String criteria);
}
