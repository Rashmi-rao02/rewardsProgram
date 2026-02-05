package com.retailer.reward.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;


@Data
@AllArgsConstructor
public class RewardSummaryResponse {
    private List<RewardResponse> customerRewards;
    private int grandTotalPoints;
    private LocalDate reportStartDate;
    private LocalDate reportEndDate;
}