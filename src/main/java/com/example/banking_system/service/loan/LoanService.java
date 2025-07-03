package com.example.banking_system.service.loan; // CORRECTED PACKAGE

import com.example.banking_system.model.loan.Loan; // CORRECTED IMPORT
import com.example.banking_system.repository.loan.LoanRepository; // CORRECTED IMPORT
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * LoanService provides business logic for managing loans.
 */
@Service
public class LoanService {

    private final LoanRepository loanRepository;

    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    /**
     * Retrieves all loans for a given user.
     * @param userId The ID of the user.
     * @return A list of Loan objects.
     */
    public List<Loan> getAllLoansForUser(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    /**
     * Retrieves a specific loan by its ID and user ID.
     * @param id The ID of the loan.
     * @param userId The ID of the user.
     * @return An Optional containing the Loan if found and belongs to the user.
     */
    public Optional<Loan> getLoanByIdForUser(Long id, Long userId) {
        return loanRepository.findByIdAndUserId(id, userId);
    }

    /**
     * Creates a new loan application.
     * @param loan The Loan object to be created.
     * @return The created Loan object with its generated ID.
     */
    public Loan createLoan(Loan loan) {
        if (loan.getStatus() == null || loan.getStatus().isEmpty()) {
            loan.setStatus("PENDING"); // Default status for new loan applications
        }
        return loanRepository.save(loan);
    }

    /**
     * Updates an existing loan.
     * @param id The ID of the loan to update.
     * @param userId The ID of the user who owns the loan.
     * @param updatedLoan The Loan object with updated details.
     * @return An Optional containing the updated Loan if found, otherwise empty.
     */
    public Optional<Loan> updateLoan(Long id, Long userId, Loan updatedLoan) {
        Optional<Loan> existingLoanOptional = getLoanByIdForUser(id, userId);
        if (existingLoanOptional.isPresent()) {
            Loan existingLoan = existingLoanOptional.get();
            // Update fields that are allowed to be changed
            existingLoan.setLoanAmount(updatedLoan.getLoanAmount());
            existingLoan.setInterestRate(updatedLoan.getInterestRate());
            existingLoan.setTermMonths(updatedLoan.getTermMonths());
            existingLoan.setStartDate(updatedLoan.getStartDate());
            existingLoan.setDueDate(updatedLoan.getDueDate());
            existingLoan.setStatus(updatedLoan.getStatus()); // Allows status updates by admin/system

            return Optional.of(loanRepository.save(existingLoan));
        }
        return Optional.empty();
    }

    /**
     * Marks a loan as approved.
     * This is typically an admin action.
     * @param id The ID of the loan.
     * @return True if status updated, false otherwise.
     */
    public boolean approveLoan(Long id) {
        Optional<Loan> loanOptional = loanRepository.findById(id);
        if (loanOptional.isPresent()) {
            Loan loan = loanOptional.get();
            loan.setStatus("APPROVED");
            loanRepository.save(loan);
            return true;
        }
        return false;
    }

    /**
     * Marks a loan as rejected.
     * This is typically an admin action.
     * @param id The ID of the loan.
     * @return True if status updated, false otherwise.
     */
    public boolean rejectLoan(Long id) {
        Optional<Loan> loanOptional = loanRepository.findById(id);
        if (loanOptional.isPresent()) {
            Loan loan = loanOptional.get();
            loan.setStatus("REJECTED");
            loanRepository.save(loan);
            return true;
        }
        return false;
    }

    /**
     * Marks a loan as paid.
     * @param id The ID of the loan.
     * @param userId The ID of the user who owns the loan.
     * @return True if status updated, false otherwise.
     */
    public boolean markLoanAsPaid(Long id, Long userId) {
        Optional<Loan> existingLoanOptional = getLoanByIdForUser(id, userId);
        if (existingLoanOptional.isPresent()) {
            Loan existingLoan = existingLoanOptional.get();
            existingLoan.setStatus("PAID");
            loanRepository.save(existingLoan);
            return true;
        }
        return false;
    }

    /**
     * Deletes a loan by its ID and user ID.
     * @param id The ID of the loan to delete.
     * @param userId The ID of the user who owns the loan.
     * @return True if deleted, false otherwise.
     */
    public boolean deleteLoan(Long id, Long userId) {
        Optional<Loan> existingLoanOptional = getLoanByIdForUser(id, userId);
        if (existingLoanOptional.isPresent()) {
            loanRepository.deleteById(id);
            return true;
        }
        return false;
    }
}