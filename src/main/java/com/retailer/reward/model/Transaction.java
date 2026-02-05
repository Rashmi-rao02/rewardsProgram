package com.retailer.reward.model;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @NotNull(message = "CustomerId is required")
    private Long customerId;

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotNull(message = "Transaction date is required")
    private LocalDate date;
}
