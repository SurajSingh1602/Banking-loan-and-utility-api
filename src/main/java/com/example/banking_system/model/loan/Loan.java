package com.example.banking_system.model.loan;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

/**
 * Loan entity representing a loan taken by a user.
 */
@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // Foreign key to the User

    @Column(nullable = false)
    private Double loanAmount;

    @Column(nullable = false)
    private Double interestRate; // e.g., 0.05 for 5%

    @Column(nullable = false)
    private Integer termMonths; // Loan term in months

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate dueDate; // Final due date of the loan

    @Column(nullable = false)
    private String status; // e.g., "PENDING", "APPROVED", "REJECTED", "ACTIVE", "PAID", "OVERDUE"

    // You could add fields for monthly payment amount, outstanding balance, etc.
}