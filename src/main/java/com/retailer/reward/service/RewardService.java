package com.retailer.reward.service;

import com.retailer.reward.dto.*;
import com.retailer.reward.model.Transaction;
import com.retailer.reward.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RewardService {

    @Autowired
    private TransactionRepository repository;

    private static void validateDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) throw new IllegalArgumentException("Invalid Request: Dates are required.");
        if (start.isAfter(end)) throw new IllegalArgumentException("Invalid Request: Start date after End date.");
        if (end.isAfter(LocalDate.now())) throw new IllegalArgumentException("Invalid Request: Future dates not allowed.");
    }

    public int calculatePoints(double amount) {
        if (amount < 0) throw new IllegalArgumentException("Invalid Request: Amount cannot be negative.");
        int points = 0;
        if (amount > 100) points = (int) (amount - 100) * 2 + 50;
        else if (amount > 50) points = (int) (amount - 50);
        return points;
    }

    public List<RewardResponse> getRewardsReport(LocalDate start, LocalDate end) {
        validateDateRange(start, end);
        List<Transaction> transactions = repository.findAllByDateBetween(start, end);

        return transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCustomerId,
                        Collectors.groupingBy(t -> t.getDate().getMonth(),
                                Collectors.summingInt(t -> calculatePoints(t.getAmount())))
                ))
                .entrySet().stream()
                .map(e -> new RewardResponse(e.getKey(), e.getValue()))
                .toList();
    }

    public RewardSummaryResponse getRecentRewardsSummary(int months) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(months);
        List<RewardResponse> responses = getRewardsReport(start, end);
        int total = responses.stream().mapToInt(RewardResponse::getTotalPoints).sum();
        return new RewardSummaryResponse(responses, total, start, end);
    }
}