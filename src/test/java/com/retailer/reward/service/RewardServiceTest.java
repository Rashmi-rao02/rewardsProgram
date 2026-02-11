package com.retailer.reward.service;

import com.retailer.reward.dto.RewardResponse;
import com.retailer.reward.dto.RewardSummaryResponse;
import com.retailer.reward.model.Transaction;
import com.retailer.reward.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // Ensures database is rolled back after every test
class RewardServiceTest {

    @Autowired
    private RewardService rewardService;

    @Autowired
    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll(); //Ensuring a clean slate for every test
    }


    @Test
    @DisplayName("Isolation - Ensure points are not mixed between different customers")
    void testCustomerIsolation() {
        LocalDate now = LocalDate.now();
        repository.save(new Transaction(1L, new BigDecimal("120.00"), now)); // 90 pts
        repository.save(new Transaction(2L, new BigDecimal("120.00"), now)); // 90 pts

        List<RewardResponse> report = rewardService.getRewardsReport(now.minusDays(1), now);

        assertEquals(2, report.size());
        report.forEach(r -> assertEquals(90, r.getTotalPoints()));
    }


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
    void testCalculatePoints(String amount, int expectedPoints) {
        assertEquals(expectedPoints, rewardService.calculatePoints(new BigDecimal(amount)));
    }


    @Test
    @DisplayName("Should aggregate points by customer and month from H2 database")
    void testGetRewardsReport_Integration() {
        LocalDate today = LocalDate.now();
        LocalDate lastMonth = today.minusMonths(1);

        // Given: Multiple transactions for the same customer in different months
        repository.save(new Transaction(1L, new BigDecimal(120.0), today));      // 90 pts
        repository.save(new Transaction(1L, new BigDecimal(101.0), lastMonth));  // 52 pts
        repository.save(new Transaction(2L, new BigDecimal(50.01), today));      // 0 pts (Edge case)

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
        assertThrows(IllegalArgumentException.class, () -> rewardService.calculatePoints(new BigDecimal(-100.0)));
    }

    @Test
    @DisplayName("Should throw exception when start date is after end date")
    void testInvalidDateRange() {
        LocalDate today = OffsetDateTime.now(RewardService.EVALUATION_ZONE).toLocalDate();
        assertThrows(IllegalArgumentException.class, () ->
                rewardService.getRewardsReport(today, today.minusDays(1)));
    }

    @Test
    @DisplayName("Should throw exception for future dates")
    void testFutureDateRange() {
        LocalDate serviceToday = OffsetDateTime.now(RewardService.EVALUATION_ZONE).toLocalDate();
        LocalDate future = serviceToday.plusDays(1);
        assertThrows(IllegalArgumentException.class, () ->
                rewardService.getRewardsReport(serviceToday, future));
    }

    @Test
    @DisplayName("Report - Return empty list when no transactions exist")
    void testGetRewardsReport_Empty() {
        List<RewardResponse> result = rewardService.getRewardsReport(LocalDate.now().minusDays(5), LocalDate.now());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Report - Throw exception on null dates")
    void testGetRewardsReport_NullDates() {
        assertThrows(IllegalArgumentException.class, () -> rewardService.getRewardsReport(null, null));
    }

    @Test
    @DisplayName("Aggregation - Multiple transactions for one customer in one month")
    void testAggregation_SameMonth() {
        Long customerId = 100L;
        LocalDate date = LocalDate.of(2023, Month.JANUARY, 1);

        // 120.00 = 90 pts | 60.00 = 10 pts | Total = 100 pts
        repository.save(new Transaction(customerId, new BigDecimal("120.00"), date));
        repository.save(new Transaction(customerId, new BigDecimal("60.00"), date));

        List<RewardResponse> report = rewardService.getRewardsReport(date, date.plusDays(5));

        assertEquals(1, report.size());
        assertEquals(100, report.get(0).getTotalPoints());
        assertEquals(100, report.get(0).getMonthlyPoints().get(Month.JANUARY));
    }

    @Test
    @DisplayName("Performance - Calculate rewards for 1000 transactions")
    void testLargeDataset() {
        LocalDate date = LocalDate.now();
        IntStream.range(0, 1000).forEach(i ->
                repository.save(new Transaction(1L, new BigDecimal("110.00"), date)) // 70 pts each
        );

        List<RewardResponse> report = rewardService.getRewardsReport(date, date);
        assertEquals(70000, report.get(0).getTotalPoints());
    }

    @Test
    @DisplayName("Recent - Validate range and zero/negative months")
    void testRecentRewardsSummary_Validation() {

        LocalDate expectedUtcToday = OffsetDateTime.now(RewardService.EVALUATION_ZONE).toLocalDate();

        RewardSummaryResponse zeroResponse = rewardService.getRecentRewardsSummary(0);

        assertEquals(expectedUtcToday, zeroResponse.getReportEndDate(), "End date should match UTC 'now'");
        assertEquals(expectedUtcToday, zeroResponse.getReportStartDate(), "Start date with 0 months should match UTC 'now'");


        // Very large months
        RewardSummaryResponse largeResponse = rewardService.getRecentRewardsSummary(1200); // 100 years
        assertTrue(largeResponse.getReportStartDate().isBefore(LocalDate.now().minusYears(10)));
    }

    @Test
    @DisplayName("Recent - Ensure data is correctly filtered for recent window")
    void testRecentRewardsSummary_DataFilter() {
        LocalDate now = LocalDate.now();
        repository.save(new Transaction(1L, new BigDecimal("100.00"), now)); // Inside 3 months
        repository.save(new Transaction(1L, new BigDecimal("100.00"), now.minusMonths(6))); // Outside

        RewardSummaryResponse summary = rewardService.getRecentRewardsSummary(3);
        assertEquals(1, summary.getCustomerRewards().size());
        assertEquals(50, summary.getGrandTotalPoints());
    }

    @Test
    @DisplayName("Date Logic - Handle transactions on Leap Day (Feb 29)")
    void testLeapYearHandling() {
        LocalDate leapDay = LocalDate.of(2024, 2, 29);
        repository.save(new Transaction(1L, new BigDecimal("120.00"), leapDay));

        List<RewardResponse> report = rewardService.getRewardsReport(leapDay, leapDay);
        assertEquals(1, report.size());
        assertEquals(90, report.get(0).getMonthlyPoints().get(Month.FEBRUARY));
    }


}