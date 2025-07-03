package com.example.banking_system.service.banking; // CORRECTED PACKAGE

import com.example.banking_system.model.banking.BankAccount; // CORRECTED IMPORT
import com.example.banking_system.model.banking.Transaction; // CORRECTED IMPORT
import com.example.banking_system.repository.banking.BankAccountRepository; // CORRECTED IMPORT
import com.example.banking_system.repository.banking.TransactionRepository; // CORRECTED IMPORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * BankAccountService provides business logic for managing bank accounts and transactions.
 */
@Service
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    public BankAccountService(BankAccountRepository bankAccountRepository, TransactionRepository transactionRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Retrieves all bank accounts for a given user.
     * @param userId The ID of the user.
     * @return A list of BankAccount objects.
     */
    public List<BankAccount> getAllAccountsForUser(Long userId) {
        return bankAccountRepository.findByUserId(userId);
    }

    /**
     * Retrieves a specific bank account by its ID and user ID.
     * @param id The ID of the bank account.
     * @param userId The ID of the user.
     * @return An Optional containing the BankAccount if found and belongs to the user.
     */
    public Optional<BankAccount> getAccountByIdForUser(Long id, Long userId) {
        return bankAccountRepository.findByIdAndUserId(id, userId);
    }

    /**
     * Creates a new bank account.
     * @param bankAccount The BankAccount object to be created.
     * @return The created BankAccount object with its generated ID.
     * @throws IllegalArgumentException if the account number already exists.
     */
    public BankAccount createAccount(BankAccount bankAccount) {
        if (bankAccountRepository.findByAccountNumber(bankAccount.getAccountNumber()).isPresent()) {
            throw new IllegalArgumentException("Account number already exists.");
        }
        if (bankAccount.getBalance() == null) {
            bankAccount.setBalance(0.0); // Default to 0 balance if not provided
        }
        return bankAccountRepository.save(bankAccount);
    }

    /**
     * Deposits money into a specified bank account.
     * @param accountId The ID of the account to deposit into.
     * @param userId The ID of the user who owns the account.
     * @param amount The amount to deposit.
     * @return The updated BankAccount, or Optional.empty() if account not found.
     * @throws IllegalArgumentException if amount is negative or account not found.
     */
    @Transactional
    public Optional<BankAccount> deposit(Long accountId, Long userId, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        Optional<BankAccount> accountOptional = getAccountByIdForUser(accountId, userId);
        if (accountOptional.isPresent()) {
            BankAccount account = accountOptional.get();
            account.setBalance(account.getBalance() + amount);
            bankAccountRepository.save(account);

            // Record transaction
            Transaction transaction = new Transaction();
            transaction.setAccountId(accountId);
            transaction.setType("DEPOSIT");
            transaction.setAmount(amount);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setDescription("Deposit to account " + account.getAccountNumber());
            transactionRepository.save(transaction);

            return Optional.of(account);
        }
        return Optional.empty();
    }

    /**
     * Withdraws money from a specified bank account.
     * @param accountId The ID of the account to withdraw from.
     * @param userId The ID of the user who owns the account.
     * @param amount The amount to withdraw.
     * @return The updated BankAccount, or Optional.empty() if account not found.
     * @throws IllegalArgumentException if amount is negative or insufficient balance.
     */
    @Transactional
    public Optional<BankAccount> withdraw(Long accountId, Long userId, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        Optional<BankAccount> accountOptional = getAccountByIdForUser(accountId, userId);
        if (accountOptional.isPresent()) {
            BankAccount account = accountOptional.get();
            if (account.getBalance() < amount) {
                throw new IllegalArgumentException("Insufficient balance.");
            }
            account.setBalance(account.getBalance() - amount);
            bankAccountRepository.save(account);

            // Record transaction
            Transaction transaction = new Transaction();
            transaction.setAccountId(accountId);
            transaction.setType("WITHDRAWAL");
            transaction.setAmount(amount);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setDescription("Withdrawal from account " + account.getAccountNumber());
            transactionRepository.save(transaction);

            return Optional.of(account);
        }
        return Optional.empty();
    }

    /**
     * Transfers money between two bank accounts of the same user.
     * @param fromAccountId The ID of the source account.
     * @param toAccountId The ID of the destination account.
     * @param userId The ID of the user who owns both accounts.
     * @param amount The amount to transfer.
     * @return Optional.empty() if accounts not found or transfer fails.
     * @throws IllegalArgumentException if amount is negative, insufficient balance, or accounts are invalid.
     */
    @Transactional
    public Optional<List<BankAccount>> transfer(Long fromAccountId, Long toAccountId, Long userId, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account.");
        }

        Optional<BankAccount> fromAccountOptional = getAccountByIdForUser(fromAccountId, userId);
        Optional<BankAccount> toAccountOptional = getAccountByIdForUser(toAccountId, userId);

        if (fromAccountOptional.isPresent() && toAccountOptional.isPresent()) {
            BankAccount fromAccount = fromAccountOptional.get();
            BankAccount toAccount = toAccountOptional.get();

            if (fromAccount.getBalance() < amount) {
                throw new IllegalArgumentException("Insufficient balance in source account.");
            }

            fromAccount.setBalance(fromAccount.getBalance() - amount);
            toAccount.setBalance(toAccount.getBalance() + amount);

            bankAccountRepository.save(fromAccount);
            bankAccountRepository.save(toAccount);

            // Record transaction for source account
            Transaction fromTransaction = new Transaction();
            fromTransaction.setAccountId(fromAccountId);
            fromTransaction.setType("TRANSFER_OUT");
            fromTransaction.setAmount(amount);
            fromTransaction.setTransactionDate(LocalDateTime.now());
            fromTransaction.setDescription("Transfer to account " + toAccount.getAccountNumber());
            transactionRepository.save(fromTransaction);

            // Record transaction for destination account
            Transaction toTransaction = new Transaction();
            toTransaction.setAccountId(toAccountId);
            toTransaction.setType("TRANSFER_IN");
            toTransaction.setAmount(amount);
            toTransaction.setTransactionDate(LocalDateTime.now());
            toTransaction.setDescription("Transfer from account " + fromAccount.getAccountNumber());
            transactionRepository.save(toTransaction);

            return Optional.of(List.of(fromAccount, toAccount));
        }
        return Optional.empty();
    }

    /**
     * Retrieves all transactions for a specific bank account belonging to a user.
     * @param accountId The ID of the bank account.
     * @param userId The ID of the user who owns the account.
     * @return A list of Transaction objects.
     */
    public List<Transaction> getTransactionsForAccount(Long accountId, Long userId) {
        // First, verify the account belongs to the user
        if (bankAccountRepository.findByIdAndUserId(accountId, userId).isEmpty()) {
            throw new IllegalArgumentException("Account not found or does not belong to the user.");
        }
        return transactionRepository.findByAccountId(accountId);
    }
}