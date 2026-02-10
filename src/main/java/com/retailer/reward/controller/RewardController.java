package com.retailer.reward.controller;

import com.retailer.reward.dto.*;
import com.retailer.reward.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reward")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    @GetMapping("/calculate")
    public List<RewardResponse> calculate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return rewardService.getRewardsReport(start, end);
    }

    @GetMapping("/recent")
    public RewardSummaryResponse getRecent(@RequestParam(defaultValue = "3") int months) {
        return rewardService.getRecentRewardsSummary(months);
    }
}