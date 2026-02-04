package com.retailer.reward.service;

import com.retailer.reward.dto.RewardResponse;
import com.retailer.reward.model.Transaction;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RewardServiceTest {

    private final RewardService rewardService = new RewardService();


    @Test
    void testGetRewardsReport_WithDateFiltering() {
        // Arrange
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        List<Transaction> transactions = List.of(
                // In range: $120 -> 90 points
                new Transaction(1L, 120.0, LocalDate.of(2025, 1, 15)),
                // Out of range: Should be ignored
                new Transaction(1L, 500.0, LocalDate.of(2025, 2, 10))
        );

        // Act
        List<RewardResponse> report = rewardService.getRewardsReport(transactions, start, end);

        // Assert
        assertThat(report).hasSize(1);
        Map<Month, Integer> monthlyMap = report.get(0).getMonthlyPoints();
        assertThat(monthlyMap.get(Month.JANUARY)).isEqualTo(90);
        assertThat(monthlyMap).doesNotContainKey(Month.FEBRUARY);
    }

    @Test
    void testGetRewardsReport_MultiMonthAggregation() {
        // Arrange
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 3, 31);
        Long customerId = 1L;

        List<Transaction> transactions = List.of(
                new Transaction(customerId, 120.0, LocalDate.of(2025, 1, 10)), // 90 pts
                new Transaction(customerId, 80.0, LocalDate.of(2025, 2, 10))   // 30 pts
        );

        // Act
        List<RewardResponse> report = rewardService.getRewardsReport(transactions, start, end);

        // Assert
        assertThat(report).hasSize(1);
        assertThat(report.get(0).getMonthlyPoints().get(Month.JANUARY)).isEqualTo(90);
        assertThat(report.get(0).getMonthlyPoints().get(Month.FEBRUARY)).isEqualTo(30);
    }

    @Test
    void testInvalidDateRange_ThrowsException() {
        // Start date is AFTER end date
        LocalDate start = LocalDate.of(2025, 12, 1);
        LocalDate end = LocalDate.of(2025, 1, 1);
        List<Transaction> transactions = List.of(new Transaction(1L, 100.0, LocalDate.now()));

        assertThrows(IllegalArgumentException.class, () -> {
            rewardService.getRewardsReport(transactions, start, end);
        });
    }

    @Test
    void testIncompleteData_ThrowsException() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        // Transaction with null amount
        List<Transaction> badTransactions = List.of(new Transaction(1L, null, LocalDate.now()));

        assertThrows(IllegalArgumentException.class, () -> {
            rewardService.getRewardsReport(badTransactions, start, end);
        });
    }
}