package com.example.banking_system.repository.banking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.banking_system.model.banking.Transaction;

import java.util.List;

/**
 * TransactionRepository interface for performing CRUD operations on Transaction entities.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds all transactions associated with a specific bank account.
     * @param accountId The ID of the bank account.
     * @return A list of Transaction objects.
     */
    List<Transaction> findByAccountId(Long accountId);
}
