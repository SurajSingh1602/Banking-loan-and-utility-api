package com.example.banking_system.model.banking;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Transaction entity representing a financial transaction on a bank account.
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId; // Foreign key to the BankAccount

    @Column(nullable = false)
    private String type; // e.g., "DEPOSIT", "WITHDRAWAL", "TRANSFER"

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime transactionDate; // Timestamp of the transaction

    private String description; // Optional description for the transaction
}
