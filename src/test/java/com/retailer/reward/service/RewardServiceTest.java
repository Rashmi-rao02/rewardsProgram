package com.retailer.reward.service;

import com.retailer.reward.dto.RewardResponse;
import com.retailer.reward.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RewardServiceTest {

    private final RewardService rewardService = new RewardService();
    private final LocalDate JAN_1 = LocalDate.of(2025, 1, 1);
    private final LocalDate JAN_31 = LocalDate.of(2025, 1, 31);

    @ParameterizedTest(name = "Amount {0} should yield {1} points")
    @CsvSource({
            "50.0, 0", "80.0, 30", "100.0, 50", "120.0, 90"
    })
    void testPointsLogic(double amount, int expectedPoints) {
        assertThat(rewardService.calculatePoints(amount)).isEqualTo(expectedPoints);
    }

    @Test
    @DisplayName("Should correctly filter dates and aggregate by customer")
    void testAggregationAndFiltering() {
        List<Transaction> transactions = List.of(
                new Transaction(1L, 120.0, JAN_1),           // In range: 90 pts
                new Transaction(1L, 50.0, JAN_1.plusDays(1)), // In range: 0 pts
                new Transaction(1L, 100.0, JAN_31.plusDays(1)) // Out of range
        );

        List<RewardResponse> report = rewardService.getRewardsReport(transactions, JAN_1, JAN_31);

        assertThat(report).hasSize(1);
        assertThat(report.get(0).getTotalPoints()).isEqualTo(90);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for any invalid input")
    void testInvalidInputs() {
        // Bad Date Range (Start after End)
        assertThrows(IllegalArgumentException.class, () ->
                rewardService.getRewardsReport(List.of(), JAN_31, JAN_1));

        // Missing Data (Null Amount)
        List<Transaction> badData = List.of(new Transaction(1L, null, JAN_1));
        assertThrows(IllegalArgumentException.class, () ->
                rewardService.getRewardsReport(badData, JAN_1, JAN_31));
    }

    @Test
    @DisplayName("Should throw error when transaction list is empty")
    void testEmptyHandling() {
        assertThrows(IllegalArgumentException.class, () ->
                rewardService.getRewardsReport(List.of(), JAN_1, JAN_31)
        );
    }
}