package com.retailer.reward.repository;

import com.retailer.reward.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repository;

    @Test
    @DisplayName("Repository - Find transactions within inclusive date range")
    void testFindAllByDateBetween() {
        LocalDate start = LocalDate.of(2023, 1, 1);
        LocalDate end = LocalDate.of(2023, 1, 31);

        repository.save(new Transaction(1L, new BigDecimal("100"), start));
        repository.save(new Transaction(1L, new BigDecimal("100"), end));
        repository.save(new Transaction(1L, new BigDecimal("100"), start.minusDays(1))); // Outside

        List<Transaction> result = repository.findAllByDateBetween(start, end);

        assertEquals(2, result.size(), "Should only find the 2 transactions within the range");
    }
}