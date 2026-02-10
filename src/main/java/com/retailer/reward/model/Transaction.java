package com.retailer.reward.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "CustomerId is required")
    private Long customerId;

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotNull(message = "Transaction date is required")
    private LocalDate date;


    public Transaction(Long customerId, Double amount, LocalDate date) {
        this.customerId = customerId;
        this.amount = amount;
        this.date = date;
    }

}