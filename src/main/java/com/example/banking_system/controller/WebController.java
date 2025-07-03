package com.example.banking_system.controller; // CORRECTED PACKAGE

import com.example.banking_system.model.User; // CORRECTED IMPORT
import com.example.banking_system.model.UserRegistrationDto; // CORRECTED IMPORT
import com.example.banking_system.model.banking.BankAccount; // CORRECTED IMPORT
import com.example.banking_system.model.banking.Transaction; // CORRECTED IMPORT
import com.example.banking_system.model.loan.Loan; // CORRECTED IMPORT
import com.example.banking_system.model.utilitybill.UtilityBill; // CORRECTED IMPORT
import com.example.banking_system.service.UserService; // CORRECTED IMPORT
import com.example.banking_system.service.banking.BankAccountService; // CORRECTED IMPORT
import com.example.banking_system.service.loan.LoanService;       // CORRECTED IMPORT
import com.example.banking_system.service.utilitybill.UtilityBillService; // CORRECTED IMPORT
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * WebController handles requests for the web (Thymeleaf) frontend.
 * It serves HTML pages for login, registration, and utility bill, banking, and loan management.
 */
@Controller
public class WebController {

    private final UserService userService;
    private final UtilityBillService utilityBillService;
    private final BankAccountService bankAccountService;
    private final LoanService loanService;

    public WebController(UserService userService, UtilityBillService utilityBillService,
                         BankAccountService bankAccountService, LoanService loanService) {
        this.userService = userService;
        this.utilityBillService = utilityBillService;
        this.bankAccountService = bankAccountService;
        this.loanService = loanService;
    }

    /**
     * Helper method to get the authenticated user's ID.
     * @return The ID of the authenticated user.
     * @throws IllegalStateException if the authenticated user is not found in the database.
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            return userService.findByUsername(username)
                    .map(User::getId)
                    .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + username));
        }
        return null;
    }

    // --- Authentication and Registration ---

    @GetMapping("/")
    public String redirect()
    {
    	return "redirect:login";
    }
    
    @GetMapping("/login")
    public String login(Model model, @RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout) {
        if (error != null) {
            model.addAttribute("loginError", "Invalid username or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("logoutSuccess", "You have been logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("userRegistrationDto", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserRegistrationDto userRegistrationDto, RedirectAttributes redirectAttributes) {
        try {
            userService.registerNewUser(userRegistrationDto);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    // --- Utility Bills Management ---

    @GetMapping("/bills")
    public String showBills(Model model) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        List<UtilityBill> bills = utilityBillService.getAllBillsForUser(userId);
        model.addAttribute("bills", bills);
        model.addAttribute("newBill", new UtilityBill());
        return "bills";
    }

    @PostMapping("/bills/add")
    public String addBill(@ModelAttribute UtilityBill newBill, RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            newBill.setUserId(userId);
            if (newBill.getStatus() == null || newBill.getStatus().isEmpty()) {
                newBill.setStatus("UNPAID");
            }
            utilityBillService.createBill(newBill);
            redirectAttributes.addFlashAttribute("successMessage", "Bill added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding bill: " + e.getMessage());
        }
        return "redirect:/bills";
    }

    @PostMapping("/bills/pay/{id}")
    public String payBill(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        boolean paid = utilityBillService.payBill(id, userId);
        if (paid) {
            redirectAttributes.addFlashAttribute("successMessage", "Bill marked as paid.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Bill not found or could not be paid.");
        }
        return "redirect:/bills";
    }

    @PostMapping("/bills/delete/{id}")
    public String deleteBill(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        boolean deleted = utilityBillService.deleteBill(id, userId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Bill deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Bill not found or could not be deleted.");
        }
        return "redirect:/bills";
    }

    // --- Banking Accounts Management ---

    @GetMapping("/accounts")
    public String showAccounts(Model model) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        List<BankAccount> accounts = bankAccountService.getAllAccountsForUser(userId);
        model.addAttribute("accounts", accounts);
        model.addAttribute("newAccount", new BankAccount()); // For add new account form
        model.addAttribute("transaction", new Transaction()); // For deposit/withdrawal forms
        return "accounts";
    }

    @PostMapping("/accounts/add")
    public String addAccount(@ModelAttribute BankAccount newAccount, RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            newAccount.setUserId(userId);
            bankAccountService.createAccount(newAccount);
            redirectAttributes.addFlashAttribute("successMessage", "Account added successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding account: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/accounts";
    }

    @PostMapping("/accounts/{accountId}/deposit")
    public String deposit(@PathVariable Long accountId, @RequestParam Double amount, RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            bankAccountService.deposit(accountId, userId, amount);
            redirectAttributes.addFlashAttribute("successMessage", "Deposit successful!");
        }
        catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Deposit error: " + e.getMessage());
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred during deposit: " + e.getMessage());
        }
        return "redirect:/accounts";
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public String withdraw(@PathVariable Long accountId, @RequestParam Double amount, RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            bankAccountService.withdraw(accountId, userId, amount);
            redirectAttributes.addFlashAttribute("successMessage", "Withdrawal successful!");
        }
        catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Withdrawal error: " + e.getMessage());
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred during withdrawal: " + e.getMessage());
        }
        return "redirect:/accounts";
    }

    @PostMapping("/accounts/transfer")
    public String transfer(@RequestParam Long fromAccountId, @RequestParam Long toAccountId, @RequestParam Double amount, RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            bankAccountService.transfer(fromAccountId, toAccountId, userId, amount);
            redirectAttributes.addFlashAttribute("successMessage", "Transfer successful!");
        }
        catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Transfer error: " + e.getMessage());
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred during transfer: " + e.getMessage());
        }
        return "redirect:/accounts";
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public String showTransactions(@PathVariable Long accountId, Model model, RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            List<Transaction> transactions = bankAccountService.getTransactionsForAccount(accountId, userId);
            model.addAttribute("transactions", transactions);
            model.addAttribute("accountId", accountId); // Pass accountId to the view
            return "transactions";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/accounts"; // Redirect back if account not found or not owned
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/accounts";
        }
    }


    // --- Loan Management ---

    @GetMapping("/loans")
    public String showLoans(Model model) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        List<Loan> loans = loanService.getAllLoansForUser(userId);
        model.addAttribute("loans", loans);
        model.addAttribute("newLoan", new Loan()); // For add new loan form
        return "loans";
    }

    @PostMapping("/loans/add")
    public String addLoan(@ModelAttribute Loan newLoan, RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            newLoan.setUserId(userId);
            loanService.createLoan(newLoan);
            redirectAttributes.addFlashAttribute("successMessage", "Loan application submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error submitting loan application: " + e.getMessage());
        }
        return "redirect:/loans";
    }

    @PostMapping("/loans/pay/{id}")
    public String payLoan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        boolean paid = loanService.markLoanAsPaid(id, userId);
        if (paid) {
            redirectAttributes.addFlashAttribute("successMessage", "Loan marked as paid.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Loan not found or could not be paid.");
        }
        return "redirect:/loans";
    }

    @PostMapping("/loans/delete/{id}")
    public String deleteLoan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        boolean deleted = loanService.deleteLoan(id, userId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Loan deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Loan not found or could not be deleted.");
        }
        return "redirect:/loans";
    }
}