package com.capstone.healthcare.diagnosticcenter.service;

import com.capstone.healthcare.diagnosticcenter.dto.SuggestedPriceResponse;
import com.capstone.healthcare.diagnosticcenter.dto.TestPriceEntry;

import java.util.List;

public interface ICenterPricingService {

    /** Returns all centers that offer testId, sorted by price ascending. */
    List<TestPriceEntry> getPricesForTest(int testId);

    /**
     * Suggested price for centerId offering testId (avg of peers, or platform
     * floor).
     */
    SuggestedPriceResponse getSuggestedPrice(int centerId, int testId);

    /** Update/create the price for a center's offering of a test. */
    void updatePrice(int centerId, int testId, double price);
}
