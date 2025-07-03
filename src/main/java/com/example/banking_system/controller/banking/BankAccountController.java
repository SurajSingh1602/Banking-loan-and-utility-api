package com.example.banking_system.controller.banking; // CORRECTED PACKAGE

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
import com.example.banking_system.model.banking.BankAccount; // CORRECTED IMPORT
import com.example.banking_system.model.banking.Transaction; // CORRECTED IMPORT
import com.example.banking_system.repository.UserRepository; // CORRECTED IMPORT
import com.example.banking_system.service.banking.BankAccountService; // CORRECTED IMPORT

import java.util.List;
import java.util.Optional;

/**
 * BankAccountController handles REST API requests for bank account management.
 */
@RestController
@RequestMapping("/api/banking")
@Tag(name = "Banking Operations", description = "APIs for managing bank accounts and transactions")
public class BankAccountController {

    private final BankAccountService bankAccountService;
    private final UserRepository userRepository; // To get current user ID

    public BankAccountController(BankAccountService bankAccountService, UserRepository userRepository) {
        this.bankAccountService = bankAccountService;
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

    @Operation(summary = "Get all bank accounts for the current user",
               description = "Retrieves a list of all bank accounts associated with the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = BankAccount.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccount>> getAllBankAccounts() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<BankAccount> accounts = bankAccountService.getAllAccountsForUser(userId);
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @Operation(summary = "Get a bank account by ID for the current user",
               description = "Retrieves a single bank account by its ID, ensuring it belongs to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved account",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = BankAccount.class))),
            @ApiResponse(responseCode = "404", description = "Account not found for the user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @GetMapping("/accounts/{id}")
    public ResponseEntity<BankAccount> getBankAccountById(
            @Parameter(description = "ID of the bank account to retrieve") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<BankAccount> account = bankAccountService.getAccountByIdForUser(id, userId);
        return account.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Create a new bank account for the current user",
               description = "Creates a new bank account and associates it with the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = BankAccount.class))),
            @ApiResponse(responseCode = "400", description = "Invalid account data provided or account number exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @PostMapping("/accounts")
    public ResponseEntity<BankAccount> createBankAccount(@RequestBody BankAccount bankAccount) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            bankAccount.setUserId(userId);
            BankAccount createdAccount = bankAccountService.createAccount(bankAccount);
            return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Account number already exists
        }
    }

    @Operation(summary = "Deposit money into a bank account",
               description = "Deposits a specified amount into a bank account belonging to the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposit successful",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = BankAccount.class))),
            @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient balance"),
            @ApiResponse(responseCode = "404", description = "Account not found for the user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @PostMapping("/accounts/{id}/deposit")
    public ResponseEntity<BankAccount> deposit(
            @Parameter(description = "ID of the bank account") @PathVariable Long id,
            @Parameter(description = "Amount to deposit") @RequestParam Double amount) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            Optional<BankAccount> updatedAccount = bankAccountService.deposit(id, userId, amount);
            return updatedAccount.map(account -> new ResponseEntity<>(account, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Withdraw money from a bank account",
               description = "Withdraws a specified amount from a bank account belonging to the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Withdrawal successful",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = BankAccount.class))),
            @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient balance"),
            @ApiResponse(responseCode = "404", description = "Account not found for the user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @PostMapping("/accounts/{id}/withdraw")
    public ResponseEntity<BankAccount> withdraw(
            @Parameter(description = "ID of the bank account") @PathVariable Long id,
            @Parameter(description = "Amount to withdraw") @RequestParam Double amount) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            Optional<BankAccount> updatedAccount = bankAccountService.withdraw(id, userId, amount);
            return updatedAccount.map(account -> new ResponseEntity<>(account, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Transfer money between two bank accounts of the current user",
               description = "Transfers a specified amount between two accounts owned by the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successful",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = BankAccount.class))),
            @ApiResponse(responseCode = "400", description = "Invalid amount, insufficient balance, or invalid accounts"),
            @ApiResponse(responseCode = "404", description = "One or both accounts not found for the user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @PostMapping("/accounts/transfer")
    public ResponseEntity<List<BankAccount>> transfer(
            @Parameter(description = "ID of the source bank account") @RequestParam Long fromAccountId,
            @Parameter(description = "ID of the destination bank account") @RequestParam Long toAccountId,
            @Parameter(description = "Amount to transfer") @RequestParam Double amount) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            Optional<List<BankAccount>> updatedAccounts = bankAccountService.transfer(fromAccountId, toAccountId, userId, amount);
            return updatedAccounts.map(accounts -> new ResponseEntity<>(accounts, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Get transactions for a specific bank account of the current user",
               description = "Retrieves a list of all transactions for a given bank account, ensuring it belongs to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = Transaction.class))),
            @ApiResponse(responseCode = "400", description = "Account not found or does not belong to user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<Transaction>> getTransactionsForAccount(
            @Parameter(description = "ID of the bank account") @PathVariable Long accountId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            List<Transaction> transactions = bankAccountService.getTransactionsForAccount(accountId, userId);
            return new ResponseEntity<>(transactions, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}