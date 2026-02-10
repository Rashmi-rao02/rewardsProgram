package com.retailer.reward.controller;

import com.retailer.reward.dto.RewardSummaryResponse;
import com.retailer.reward.service.RewardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;

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
    void testCalculateEndpoint_Success() throws Exception {
        mockMvc.perform(get("/api/reward/calculate")
                        .param("start", "2023-01-01")
                        .param("end", "2023-03-31"))
                .andExpect(status().isOk());
    }

    @Test
    void testCalculateEndpoint_MissingParams_Returns400() throws Exception {
        // Missing 'end' parameter
        mockMvc.perform(get("/api/reward/calculate")
                        .param("start", "2023-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing Parameter"));
    }

    @Test
    void testRecentEndpoint_ValidatesDefaultValue() throws Exception {
        RewardSummaryResponse mockResponse = new RewardSummaryResponse(
                new ArrayList<>(), 0, LocalDate.now().minusMonths(3), LocalDate.now());

        given(rewardService.getRecentRewardsSummary(3)).willReturn(mockResponse);

        mockMvc.perform(get("/api/reward/recent")) // months defaults to 3
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotalPoints").value(0));
    }

    @Test
    void testRecentEndpoint_InvalidType_Returns400() throws Exception {
        mockMvc.perform(get("/api/reward/recent")
                        .param("months", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}