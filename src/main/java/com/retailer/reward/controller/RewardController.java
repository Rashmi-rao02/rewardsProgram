package com.retailer.reward.controller;

import com.retailer.reward.dto.*;
import com.retailer.reward.service.RewardService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reward")
@Validated
public class RewardController {

    @Autowired
    private RewardService rewardService;

    @GetMapping("/calculate")
    public List<RewardResponse> calculate(
            @RequestParam  @NotNull(message = "Start date is required")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @NotNull(message = "End date is required")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return rewardService.getRewardsReport(start, end);
    }

    @GetMapping("/recent")
    public RewardSummaryResponse getRecent(
            @RequestParam(defaultValue = "3")
            @Min(value = 1, message = "Months must be at least 1")
            @Max(value = 3, message = "Months must not exceed 3")
            int months) {
        return rewardService.getRecentRewardsSummary(months);
    }
}