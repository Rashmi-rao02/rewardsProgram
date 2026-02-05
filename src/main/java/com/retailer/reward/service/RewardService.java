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


    /**
     * Processes a list of transactions to generate rewards report.
     * Only transactions falling within the provided date range are considered.
     *
     * @param transactions Raw list of transaction data to process.
     * @param startDate    The start of the calculation window.
     * @param endDate      The end of the calculation window.
     * @return A list of RewardResponse objects containing aggregated monthly points.
     * @throws IllegalArgumentException if the date range is logically invalid or data is missing.
     */
    public List<RewardResponse> getRewardsReport(List<Transaction> transactions, LocalDate startDate, LocalDate endDate) {
        log.info("Processing rewards report: Range {} to {}", startDate, endDate);

        if (startDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the future");
        }

        //Validation of the requested timeframe
        if (startDate.isAfter(endDate)) {
            log.error("Invalid date range provided: start {} is after end {}", startDate, endDate);
            throw new IllegalArgumentException("StartDate cannot be after EndDate.");
        }

        // Ensure the reporting window does not extend into the future
        LocalDate today = LocalDate.now();
        if (endDate.isAfter(today)) {
            log.error("Invalid date range: end date {} is in the future", endDate);
            throw new IllegalArgumentException("EndDate cannot be a future date.");
        }

        Map<Long, Map<Month, Integer>> masterMap = new HashMap<>();

        for (Transaction t : transactions) {

            if (t.getCustomerId() == null || t.getDate() == null || t.getAmount() == null) {
                log.error("Validation failed: Transaction contains null values: {}", t);
                throw new IllegalArgumentException("Transaction data is incomplete: CustomerID, Date, and Amount are required");
            }


            if (t.getDate().isBefore(startDate) || t.getDate().isAfter(endDate)) {
                continue;
            }

            Long id = t.getCustomerId();
            Month month = t.getDate().getMonth();
            int points = calculatePoints(t.getAmount());

            masterMap.computeIfAbsent(id, k -> new HashMap<>())
                    .merge(month, points, Integer::sum);
        }


        List<RewardResponse> response = new ArrayList<>();
        for (var entry : masterMap.entrySet()) {
            response.add(new RewardResponse(entry.getKey(), entry.getValue()));
        }

        log.debug("Calculation complete. Found {} customers in range.", response.size());
        return response;
    }


    /**
     * Calculates reward points based on the retailer's system.
     * * Formula:
     * - 2 points for every dollar spent over $100.
     * - 1 point for every dollar spent over $50 (up to $100).
     *
     * @param amount The transaction amount.
     * @return Calculated integer points.
     */
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