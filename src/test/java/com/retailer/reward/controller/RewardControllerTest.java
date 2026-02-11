package com.retailer.reward.controller;

import com.retailer.reward.dto.RewardResponse;
import com.retailer.reward.dto.RewardSummaryResponse;
import com.retailer.reward.service.RewardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RewardController.class)
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RewardService rewardService;


    @Test
    @DisplayName("GET /calculate - Invalid date format returns Parameter Error")
    void testCalculate_InvalidDateFormat() throws Exception {
        mockMvc.perform(get("/api/reward/calculate")
                        .param("start", "2023-13-01") // Invalid Month
                        .param("end", "2023-12-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parameter Error"));
    }


    // Start Date after End Date (Business Logic)
    @Test
    @DisplayName("GET /calculate - Start date after end date returns Validation Error")
    void testCalculate_StartAfterEnd() throws Exception {
        given(rewardService.getRewardsReport(any(), any()))
                .willThrow(new IllegalArgumentException("Start date cannot be after end date."));

        mockMvc.perform(get("/api/reward/calculate")
                        .param("start", "2023-12-31")
                        .param("end", "2023-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Start date cannot be after end date."));
    }


    //Future Dates
    @Test
    @DisplayName("GET /calculate - Future dates return Validation Error")
    void testCalculate_FutureDate() throws Exception {
        given(rewardService.getRewardsReport(any(), any()))
                .willThrow(new IllegalArgumentException("Future dates not allowed."));

        mockMvc.perform(get("/api/reward/calculate")
                        .param("start", "2023-01-01")
                        .param("end", "2099-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }


    // Response Body Validation (Actual Data Structure)
    @Test
    @DisplayName("GET /calculate - Verify exact JSON structure of RewardResponse")
    void testCalculate_ResponseBodyValidation() throws Exception {
        // Mock data structure: Customer 1, JANUARY: 90 points
        RewardResponse resp = new RewardResponse(1L, Map.of(Month.JANUARY, 90));
        given(rewardService.getRewardsReport(any(), any())).willReturn(List.of(resp));

        mockMvc.perform(get("/api/reward/calculate")
                        .param("start", "2023-01-01")
                        .param("end", "2023-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].monthlyPoints.JANUARY").value(90)) // Check Map structure
                .andExpect(jsonPath("$[0].totalPoints").value(90));
    }

    // Custom Months Parameter in /recent
    @Test
    @DisplayName("GET /recent - Custom months parameter is passed to service")
    void testRecent_CustomMonths() throws Exception {
        int customMonths = 6;
        RewardSummaryResponse mockSummary = new RewardSummaryResponse(
                new ArrayList<>(), 0, LocalDate.now().minusMonths(customMonths), LocalDate.now());

        given(rewardService.getRecentRewardsSummary(customMonths)).willReturn(mockSummary);

        mockMvc.perform(get("/api/reward/recent")
                        .param("months", String.valueOf(customMonths)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportStartDate").exists());
    }

    @Test
    @DisplayName("GET /recent - Months less than 1 returns Parameter Error")
    void testRecent_InvalidMonths() throws Exception {
        mockMvc.perform(get("/api/reward/recent")
                        .param("months", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parameter Error"));
    }

    @Test
    @DisplayName("GET /calculate - Success with valid date range")
    void testCalculateEndpoint_Success() throws Exception {
        mockMvc.perform(get("/api/reward/calculate")
                        .param("start", "2023-01-01")
                        .param("end", "2023-03-31"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /calculate - Return 400 Parameter Error when end date is missing")
    void testCalculateEndpoint_MissingParams_Returns400() throws Exception {
        // Missing 'end' parameter
        mockMvc.perform(get("/api/reward/calculate")
                        .param("start", "2023-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parameter Error"));
    }

    @Test
    @DisplayName("GET /recent - Successfully use default value (3 months) when param is omitted")
    void testRecentEndpoint_ValidatesDefaultValue() throws Exception {
        RewardSummaryResponse mockResponse = new RewardSummaryResponse(
                new ArrayList<>(), 0, LocalDate.now().minusMonths(3), LocalDate.now());

        given(rewardService.getRecentRewardsSummary(3)).willReturn(mockResponse);

        mockMvc.perform(get("/api/reward/recent")) // months defaults to 3
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotalPoints").value(0));
    }

    @Test
    @DisplayName("GET /recent - Return 400 Parameter Error when months is not a numeric value")
    void testRecentEndpoint_InvalidType_Returns400() throws Exception {
        mockMvc.perform(get("/api/reward/recent")
                        .param("months", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parameter Error"));
    }
}