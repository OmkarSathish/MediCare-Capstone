package com.capstone.healthcare.diagnosticcenter.service;

import com.capstone.healthcare.diagnosticcenter.model.CenterTestOffering;

public interface ICenterCatalogService {

    CenterTestOffering viewTestOffering(int diagnosticCenterId, String testName);

    CenterTestOffering addTest(int diagnosticCenterId, int testId, double price);
}
