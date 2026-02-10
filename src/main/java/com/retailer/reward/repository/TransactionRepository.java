package com.retailer.reward.repository;

import com.retailer.reward.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByDateBetween(LocalDate start, LocalDate end);
}