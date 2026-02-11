package com.retailer.reward.exception;

import jakarta.validation.constraints.Min;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Handle IllegalArgumentException - Return Validation Error")
    void testHandleIllegalArgument() throws Exception {
        mockMvc.perform(get("/test/error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Logic Failed"));
    }

    @Test
    @DisplayName("Handle Missing Request Parameter - Return Parameter Error")
    void testHandleMissingParam() throws Exception {
        mockMvc.perform(get("/test/missing-param")) // Missing 'id'
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parameter Error"));
    }


    @Test
    @DisplayName("Handle Uncaught RuntimeException - Return Server Error")
    void testHandleGeneralException() throws Exception {
        mockMvc.perform(get("/test/fatal"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Server Error"));
    }

    // Dummy controller to trigger exceptions for testing the handler
    @RestController
    @Validated
    private static class TestController {
        @GetMapping("/test/error")
        public void triggerLogicError() { throw new IllegalArgumentException("Logic Failed"); }

        @GetMapping("/test/missing-param")
        public void triggerParamError(@RequestParam String id) {}

        @GetMapping("/test/constraint-violation")
        public void triggerConstraint(@RequestParam @Min(1) int months) {
        }

        @GetMapping("/test/fatal")
        public void triggerFatal() { throw new RuntimeException("Crash"); }
    }
}
