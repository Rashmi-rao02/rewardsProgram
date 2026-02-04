package com.retailer.reward.controller;

import com.retailer.reward.dto.RewardResponse;
import com.retailer.reward.model.Transaction;
import com.retailer.reward.service.RewardService;
import jakarta.validation.Valid;
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
            @Valid @RequestBody List<Transaction> transactions,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return rewardService.getRewardsReport(transactions, startDate, endDate);
    }


    /**
     * Provides a pre-calculated sample report.
     * * @return A list of RewardResponse based on hardcoded transaction data.
     */
    @GetMapping("/sample-data")
    public List<RewardResponse> getSampleData() {
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, 120.0, LocalDate.of(2025, 1, 15)),
                new Transaction(1L, 80.0, LocalDate.of(2023, 2, 10)),
                new Transaction(1L, 10.0, LocalDate.of(2023, 3, 5)),
                new Transaction(2L, 500.0, LocalDate.of(2023, 1, 20))
        );

        return rewardService.getRewardsReport(transactions, LocalDate.of(2023, 1, 1), LocalDate.of(2025, 12, 31));
    }

}