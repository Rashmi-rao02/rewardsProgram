package com.retailer.reward.service;

import lombok.extern.slf4j.Slf4j;
import com.retailer.reward.dto.RewardResponse;
import com.retailer.reward.model.Transaction;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RewardService {

    public List<RewardResponse> getRewardsReport(List<Transaction> transactions, LocalDate startDate, LocalDate endDate) {
        log.info("Processing rewards report: Range {} to {}", startDate, endDate);

        if (startDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the future");
        }

/*        (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }*/


        if (startDate.isAfter(endDate)) {
            log.error("Invalid date range provided: start {} is after end {}", startDate, endDate);
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }

        // Map<CustomerId, Map<Month, TotalPoints>>
        Map<Long, Map<Month, Integer>> masterMap = new HashMap<>();

        for (Transaction t : transactions) {
            // 1. Null validation
            if (t.getCustomerId() == null || t.getDate() == null || t.getAmount() == null) {
                log.error("Validation failed: Transaction contains null values: {}", t);
                throw new IllegalArgumentException("Transaction data is incomplete: CustomerID, Date, and Amount are required");
            }

            // 2. Date Range Filtering
            if (t.getDate().isBefore(startDate) || t.getDate().isAfter(endDate)) {
                continue; // Skip transactions outside the user-specified range
            }

            // 3. Calculation & Aggregation
            Long id = t.getCustomerId();
            Month month = t.getDate().getMonth();
            int points = calculatePoints(t.getAmount());

            masterMap.computeIfAbsent(id, k -> new HashMap<>())
                    .merge(month, points, Integer::sum);
        }

        // 4. Transform Map to List of Response DTOs
        List<RewardResponse> response = new ArrayList<>();
        for (var entry : masterMap.entrySet()) {
            response.add(new RewardResponse(entry.getKey(), entry.getValue()));
        }

        log.debug("Calculation complete. Found {} customers in range.", response.size());
        return response;
    }

    public int calculatePoints(double amount) {
        if (amount < 0) throw new IllegalArgumentException("Transaction amount cannot be negative");

        int points = 0;
        if (amount > 100) {
            points = (int) (amount - 100) * 2 + 50;
        } else if (amount > 50) {
            points = (int) (amount - 50);
        }

        log.trace("Amount ${} results in {} points", amount, points);
        return points;
    }
}