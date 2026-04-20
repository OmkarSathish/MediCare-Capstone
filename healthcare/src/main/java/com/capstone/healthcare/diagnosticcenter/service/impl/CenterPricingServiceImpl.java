package com.capstone.healthcare.diagnosticcenter.service.impl;

import com.capstone.healthcare.diagnosticcenter.dto.SuggestedPriceResponse;
import com.capstone.healthcare.diagnosticcenter.dto.TestPriceEntry;
import com.capstone.healthcare.diagnosticcenter.model.CenterTestOffering;
import com.capstone.healthcare.diagnosticcenter.repository.ICenterTestOfferingRepository;
import com.capstone.healthcare.diagnosticcenter.service.ICenterPricingService;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CenterPricingServiceImpl implements ICenterPricingService {

    private final ICenterTestOfferingRepository offeringRepository;

    @Override
    public List<TestPriceEntry> getPricesForTest(int testId) {
        return offeringRepository.findAllByTest_Id(testId).stream()
                .sorted(Comparator.comparingDouble(CenterTestOffering::getPrice))
                .map(o -> TestPriceEntry.builder()
                        .centerId(o.getCenter().getId())
                        .centerName(o.getCenter().getName())
                        .price(o.getPrice())
                        .build())
                .toList();
    }

    @Override
    public SuggestedPriceResponse getSuggestedPrice(int centerId, int testId) {
        double suggested = offeringRepository
                .findAveragePriceByTestIdExcludingCenter(testId, centerId)
                .filter(avg -> !Double.isNaN(avg))
                .map(avg -> Math.round(avg / 10.0) * 10.0)
                .orElseGet(() -> offeringRepository.findMinPriceAcrossAllOfferings()
                        .map(min -> Math.round(min / 10.0) * 10.0)
                        .orElse(0.0));

        boolean usedAverage = offeringRepository
                .findAveragePriceByTestIdExcludingCenter(testId, centerId)
                .filter(avg -> !Double.isNaN(avg))
                .isPresent();

        return SuggestedPriceResponse.builder()
                .suggestedPrice(suggested)
                .basis(usedAverage ? "average" : "platform_floor")
                .build();
    }

    @Override
    @Transactional
    public void updatePrice(int centerId, int testId, double price) {
        CenterTestOffering offering = offeringRepository
                .findByCenter_IdAndTest_Id(centerId, testId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Offering not found for center " + centerId + " and test " + testId));
        offering.setPrice(price);
        offeringRepository.save(offering);
    }
}
