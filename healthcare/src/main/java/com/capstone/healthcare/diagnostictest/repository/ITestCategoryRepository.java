package com.capstone.healthcare.diagnostictest.repository;

import com.capstone.healthcare.diagnostictest.model.TestCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITestCategoryRepository extends JpaRepository<TestCategory, Integer> {
    Optional<TestCategory> findByCategoryName(String categoryName);
}
