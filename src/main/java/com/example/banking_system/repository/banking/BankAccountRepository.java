package com.example.banking_system.repository.banking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.banking_system.model.banking.BankAccount;

import java.util.List;
import java.util.Optional;

/**
 * BankAccountRepository interface for performing CRUD operations on BankAccount entities.
 */
@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    /**
     * Finds all bank accounts belonging to a specific user.
     * @param userId The ID of the user.
     * @return A list of BankAccount objects.
     */
    List<BankAccount> findByUserId(Long userId);

    /**
     * Finds a bank account by its ID and ensures it belongs to a specific user.
     * @param id The ID of the bank account.
     * @param userId The ID of the user.
     * @return An Optional containing the BankAccount if found and owned by the user.
     */
    Optional<BankAccount> findByIdAndUserId(Long id, Long userId);

    /**
     * Finds a bank account by its account number.
     * @param accountNumber The account number to search for.
     * @return An Optional containing the BankAccount if found.
     */
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    /**
     * Finds a bank account by its account number and ensures it belongs to a specific user.
     * @param accountNumber The account number.
     * @param userId The ID of the user.
     * @return An Optional containing the BankAccount if found and owned by the user.
     */
    Optional<BankAccount> findByAccountNumberAndUserId(String accountNumber, Long userId);
}
