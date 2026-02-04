package com.retailer.reward.dto;

import lombok.Data;
import java.time.Month;
import java.util.Map;


@Data
public class RewardResponse {

    private Long customerId;
    private Map<Month, Integer> monthlyPoints;
    private int totalPoints;

    public RewardResponse(Long customerId, Map<Month, Integer> monthlyPoints)
    {
        this.customerId = customerId;
        this.monthlyPoints = monthlyPoints;
        this.totalPoints = monthlyPoints.values().stream().mapToInt(Integer::intValue).sum();
    }

}
