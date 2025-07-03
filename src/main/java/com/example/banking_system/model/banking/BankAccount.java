package com.example.banking_system.model.banking;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * BankAccount entity representing a user's bank account.
 */
@Entity
@Table(name = "bank_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String accountType; // e.g., "SAVINGS", "CHECKING"

    @Column(nullable = false)
    private Double balance;

    @Column(nullable = false)
    private Long userId; // Foreign key to the User

    // You can add more account details like opening date, branch, etc.
}
