package com.capstone.healthcare.diagnosticcenter.service;

import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;

import java.util.List;

public interface ICenterLookupService {

    List<DiagnosticCenter> searchCenters(String keyword);

    List<DiagnosticCenter> getCentersOfferingTest(int testId);
}
