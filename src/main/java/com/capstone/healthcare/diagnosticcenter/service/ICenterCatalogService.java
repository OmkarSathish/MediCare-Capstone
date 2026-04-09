package com.capstone.healthcare.diagnosticcenter.service;

import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;

public interface ICenterCatalogService {

    DiagnosticTest viewTestDetails(int diagnosticCenterId, String testName);

    DiagnosticTest addTest(int diagnosticCenterId, int testId);
}
