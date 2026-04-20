package com.capstone.healthcare.diagnosticcenter.repository;

import com.capstone.healthcare.diagnosticcenter.model.CenterTestOffering;
import com.capstone.healthcare.diagnosticcenter.model.CenterTestOfferingKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICenterTestOfferingRepository extends JpaRepository<CenterTestOffering, CenterTestOfferingKey> {

    List<CenterTestOffering> findAllByCenter_Id(int centerId);

    List<CenterTestOffering> findAllByTest_Id(int testId);

    Optional<CenterTestOffering> findByCenter_IdAndTest_Id(int centerId, int testId);

    Optional<CenterTestOffering> findByCenter_IdAndTest_TestName(int centerId, String testName);

    /**
     * Suggested price for a test at a given center — average of all other centers'
     * prices.
     */
    @Query("SELECT AVG(o.price) FROM CenterTestOffering o WHERE o.test.id = :testId AND o.center.id <> :excludeCenterId")
    Optional<Double> findAveragePriceByTestIdExcludingCenter(
            @Param("testId") int testId,
            @Param("excludeCenterId") int excludeCenterId);

    /**
     * Platform floor — minimum price across all offerings; used as fallback when no
     * peer prices exist.
     */
    @Query("SELECT MIN(o.price) FROM CenterTestOffering o")
    Optional<Double> findMinPriceAcrossAllOfferings();
}
