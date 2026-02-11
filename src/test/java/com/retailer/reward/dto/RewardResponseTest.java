package com.retailer.reward.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Month;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RewardResponseTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("DTO - Verify RewardResponse JSON structure")
    void testSerialization() throws Exception {
        // Given
        RewardResponse response = new RewardResponse(1L, Map.of(Month.JANUARY, 90));

        // When
        String jsonResult = mapper.writeValueAsString(response);

        // Then
        assertThat(jsonResult).contains("\"customerId\":1");
        assertThat(jsonResult).contains("\"totalPoints\":90");
        assertThat(jsonResult).contains("\"JANUARY\":90");
    }
}