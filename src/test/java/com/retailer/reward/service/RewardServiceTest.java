package com.retailer.reward.service;

import com.retailer.reward.dto.RewardResponse;
import com.retailer.reward.model.Transaction;
import com.retailer.reward.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // Ensures database is rolled back after every test
class RewardServiceTest {

    @Autowired
    private RewardService rewardService;

    @Autowired
    private TransactionRepository repository;


    @ParameterizedTest(name = "Amount {0} should result in {1} points")
    @CsvSource({
            "49.99, 0",    // Below 50
            "50.00, 0",    // Boundary 50
            "50.01, 0",    // Edge case: fractional cents don't grant a whole dollar point
            "51.00, 1",    // Exactly 1 dollar over 50
            "100.00, 50",  // Boundary 100
            "100.01, 50",  // Edge case: fractional cents don't grant 2x points
            "101.00, 52",  // 1 dollar over 100 (50 + 1*2)
            "120.00, 90",  // Standard case (50 + 20*2)
            "0.00, 0"      // Zero amount
    })
    @DisplayName("Calculate Points - Boundary and Edge Case Validation")
    void testCalculatePoints(double amount, int expectedPoints) {
        assertEquals(expectedPoints, rewardService.calculatePoints(amount));
    }


    @Test
    @DisplayName("Should aggregate points by customer and month from H2 database")
    void testGetRewardsReport_Integration() {
        LocalDate today = LocalDate.now();
        LocalDate lastMonth = today.minusMonths(1);

        // Given: Multiple transactions for the same customer in different months
        repository.save(new Transaction(1L, 120.0, today));      // 90 pts
        repository.save(new Transaction(1L, 101.0, lastMonth));  // 52 pts
        repository.save(new Transaction(2L, 50.01, today));      // 0 pts (Edge case)

        // When
        List<RewardResponse> report = rewardService.getRewardsReport(today.minusMonths(2), today);

        // Then
        assertEquals(2, report.size(), "Should have two unique customers");

        RewardResponse customer1 = report.stream()
                .filter(r -> r.getCustomerId().equals(1L))
                .findFirst().orElseThrow();

        assertEquals(142, customer1.getTotalPoints(), "Total points should be sum of 90 and 52");
        assertEquals(2, customer1.getMonthlyPoints().size(), "Should have points for two distinct months");
    }


    @Test
    @DisplayName("Should throw IllegalArgumentException for negative amounts")
    void testCalculatePoints_NegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> rewardService.calculatePoints(-100.0));
    }

    @Test
    @DisplayName("Should throw exception when start date is after end date")
    void testInvalidDateRange() {
        LocalDate today = LocalDate.now();
        assertThrows(IllegalArgumentException.class, () ->
                rewardService.getRewardsReport(today, today.minusDays(1)));
    }

    @Test
    @DisplayName("Should throw exception for future dates")
    void testFutureDateRange() {
        LocalDate future = LocalDate.now().plusDays(1);
        assertThrows(IllegalArgumentException.class, () ->
                rewardService.getRewardsReport(LocalDate.now(), future));
    }
}