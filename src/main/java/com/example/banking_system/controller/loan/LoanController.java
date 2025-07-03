package com.example.banking_system.controller.loan; // CORRECTED PACKAGE


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.example.banking_system.model.User; // CORRECTED IMPORT
import com.example.banking_system.model.loan.Loan; // CORRECTED IMPORT
import com.example.banking_system.repository.UserRepository; // CORRECTED IMPORT
import com.example.banking_system.service.loan.LoanService; // CORRECTED IMPORT

import java.util.List;
import java.util.Optional;

/**
 * LoanController handles REST API requests for loan management.
 */
@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loan Management", description = "APIs for managing loans")
public class LoanController {

    private final LoanService loanService;
    private final UserRepository userRepository; // To get current user ID

    public LoanController(LoanService loanService, UserRepository userRepository) {
        this.loanService = loanService;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                                      .orElseThrow(() -> new UsernameNotFoundException("User not found after authentication: " + username));
            return user.getId();
        }
        return null;
    }

    @Operation(summary = "Get all loans for the current user",
               description = "Retrieves a list of all loans associated with the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = Loan.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Loan> loans = loanService.getAllLoansForUser(userId);
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    @Operation(summary = "Get a loan by ID for the current user",
               description = "Retrieves a single loan by its ID, ensuring it belongs to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved loan",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = Loan.class))),
            @ApiResponse(responseCode = "404", description = "Loan not found for the user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(
            @Parameter(description = "ID of the loan to retrieve") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<Loan> loan = loanService.getLoanByIdForUser(id, userId);
        return loan.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Create a new loan application for the current user",
               description = "Creates a new loan application and associates it with the authenticated user. Initial status is PENDING.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan application created successfully",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = Loan.class))),
            @ApiResponse(responseCode = "400", description = "Invalid loan data provided"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @PostMapping
    public ResponseEntity<Loan> createLoan(@RequestBody Loan loan) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        loan.setUserId(userId);
        Loan createdLoan = loanService.createLoan(loan);
        return new ResponseEntity<>(createdLoan, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing loan for the current user",
               description = "Updates an existing loan by ID, ensuring it belongs to the authenticated user. Note: Status changes (approve/reject/pay) have dedicated endpoints.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan updated successfully",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = Loan.class))),
            @ApiResponse(responseCode = "404", description = "Loan not found for the user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Loan> updateLoan(
            @Parameter(description = "ID of the loan to update") @PathVariable Long id,
            @RequestBody Loan loan) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<Loan> updated = loanService.updateLoan(id, userId, loan);
        return updated.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Mark a loan as paid for the current user",
               description = "Changes the status of a specific loan to 'PAID'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan marked as paid successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found for the user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @PostMapping("/{id}/pay")
    public ResponseEntity<String> payLoan(
            @Parameter(description = "ID of the loan to mark as paid") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        boolean paid = loanService.markLoanAsPaid(id, userId);
        return paid ? new ResponseEntity<>("Loan marked as paid successfully!", HttpStatus.OK)
                : new ResponseEntity<>("Loan not found for the user or could not be paid.", HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Delete a loan for the current user",
               description = "Deletes a specific loan by ID, ensuring it belongs to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Loan deleted successfully (No Content)"),
            @ApiResponse(responseCode = "404", description = "Loan not found for the user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(
            @Parameter(description = "ID of the loan to delete") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        boolean deleted = loanService.deleteLoan(id, userId);
        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}