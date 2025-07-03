package com.example.banking_system.service.banking; // CORRECTED PACKAGE

import com.example.banking_system.model.banking.Transaction; // CORRECTED IMPORT
import com.example.banking_system.repository.banking.TransactionRepository; // CORRECTED IMPORT
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * TransactionService provides business logic for managing transactions.
 * It's primarily used by BankAccountService, but can be accessed directly if needed.
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Finds a transaction by its ID.
     * @param id The ID of the transaction.
     * @return An Optional containing the Transaction if found.
     */
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    /**
     * Retrieves all transactions for a given account.
     * @param accountId The ID of the account.
     * @return A list of Transaction objects.
     */
    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    /**
     * Creates a new transaction. This method is primarily called internally by BankAccountService
     * after balance updates. External calls might require more validation.
     * @param transaction The Transaction object to save.
     * @return The saved Transaction.
     */
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
}