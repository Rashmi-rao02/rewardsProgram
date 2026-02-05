package com.retailer.reward.controller;

import com.retailer.reward.dto.RewardResponse;
import com.retailer.reward.dto.RewardSummaryResponse;
import com.retailer.reward.model.Transaction;
import com.retailer.reward.service.RewardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("api/reward")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    /**
     * Calculates reward points for a list of transactions within a specific timeframe.
     * * @param transactions List of customer transactions provided in the request body.
     * @param startDate    The beginning of the reporting period (inclusive).
     * @param endDate      The end of the reporting period (inclusive).
     * @return A list of RewardResponse containing monthly and total points per customer.
     */
    @PostMapping("/calculate")
    public List<RewardResponse> calculateRewards(
            @Valid @NotEmpty(message = "Transaction list cannot be empty") @RequestBody List<Transaction> transactions,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return rewardService.getRewardsReport(transactions, startDate, endDate);
    }

    /**
     * Generates a rewards summary for a rolling window of recent activity.
     * This endpoint calculates start and end dates automatically based on the current system date.
     *
     * @param transactions List of customer transactions provided in the request body.
     * @param months       The number of months to look back from today (defaults to 3 if not specified).
     * @return A RewardSummaryResponse containing the individual customer breakdowns,
     * the grand total of points across all customers, and the calculated date range.
     */
    @PostMapping("/recent")
    public RewardSummaryResponse getRecentRewards(
            @Valid @NotEmpty(message = "Transaction list cannot be empty")
            @RequestBody List<Transaction> transactions,
            @RequestParam(value = "months", defaultValue = "3") int months) {

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);

        return createSummaryResponse(transactions, startDate, endDate);
    }

    /**
     * Provides a pre-calculated sample report using mock transaction data.
     * It uses relative dates to ensure
     * the sample data always remains within the current reporting window.
     *
     * @return A RewardSummaryResponse containing sample customer rewards,
     * the grand total, and the corresponding date range.
     */
    @GetMapping("/sample-data")
    public RewardSummaryResponse getSampleData() {
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, 120.0, LocalDate.now().minusDays(10)),
                new Transaction(1L, 80.0, LocalDate.now().minusDays(45)),
                new Transaction(2L, 500.0, LocalDate.now().minusDays(5))
        );

        return createSummaryResponse(transactions, LocalDate.now().minusMonths(3), LocalDate.now());
    }

    /**
     * Helper method to encapsulate the creation of a summary response.
     */
    private RewardSummaryResponse createSummaryResponse(List<Transaction> transactions, LocalDate start, LocalDate end) {
        List<RewardResponse> customerRewards = rewardService.getRewardsReport(transactions, start, end);

        int grandTotal = customerRewards.stream()
                .mapToInt(RewardResponse::getTotalPoints)
                .sum();

        return new RewardSummaryResponse(customerRewards, grandTotal, start, end);
    }

}