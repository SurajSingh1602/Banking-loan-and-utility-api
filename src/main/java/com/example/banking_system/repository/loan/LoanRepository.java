package com.example.banking_system.repository.loan;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.banking_system.model.loan.Loan;

import java.util.List;
import java.util.Optional;

/**
 * LoanRepository interface for performing CRUD operations on Loan entities.
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     * Finds all loans belonging to a specific user.
     * @param userId The ID of the user.
     * @return A list of Loan objects.
     */
    List<Loan> findByUserId(Long userId);

    /**
     * Finds a loan by its ID and ensures it belongs to a specific user.
     * @param id The ID of the loan.
     * @param userId The ID of the user.
     * @return An Optional containing the Loan if found and owned by the user.
     */
    Optional<Loan> findByIdAndUserId(Long id, Long userId);
}
