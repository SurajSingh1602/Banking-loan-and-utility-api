package com.example.banking_system.controller.utilitybill;


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

import com.example.banking_system.model.User;
import com.example.banking_system.model.utilitybill.UtilityBill;
import com.example.banking_system.repository.UserRepository;
import com.example.banking_system.service.utilitybill.UtilityBillService;

import java.util.List;
import java.util.Optional;

/**
 * UtilityBillController handles REST API requests for utility bill payments.
 * It exposes endpoints for creating, retrieving, updating, and deleting utility bills.
 */
@RestController
@RequestMapping("/api/utility-bills")
@Tag(name = "Utility Bill Payments", description = "APIs for managing utility bills")
public class UtilityBillController {

    private final UtilityBillService utilityBillService;
    private final UserRepository userRepository;

    public UtilityBillController(UtilityBillService utilityBillService, UserRepository userRepository) {
        this.utilityBillService = utilityBillService;
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

    @Operation(summary = "Get all utility bills for the current user",
               description = "Retrieves a list of all utility bills associated with the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = UtilityBill.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<UtilityBill>> getAllBills() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<UtilityBill> bills = utilityBillService.getAllBillsForUser(userId);
        return new ResponseEntity<>(bills, HttpStatus.OK);
    }

    @Operation(summary = "Get a utility bill by ID for the current user",
               description = "Retrieves a single utility bill by its ID, ensuring it belongs to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bill",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = UtilityBill.class))),
            @ApiResponse(responseCode = "404", description = "Bill not found for the user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UtilityBill> getBillById(
            @Parameter(description = "ID of the utility bill to retrieve") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<UtilityBill> bill = utilityBillService.getBillByIdForUser(id, userId);
        return bill.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Create a new utility bill for the current user",
               description = "Creates a new utility bill and associates it with the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bill created successfully",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = UtilityBill.class))),
            @ApiResponse(responseCode = "400", description = "Invalid bill data provided"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @PostMapping
    public ResponseEntity<UtilityBill> createBill(@RequestBody UtilityBill utilityBill) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        utilityBill.setUserId(userId);
        UtilityBill createdBill = utilityBillService.createBill(utilityBill);
        return new ResponseEntity<>(createdBill, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing utility bill for the current user",
               description = "Updates an existing utility bill by ID, ensuring it belongs to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bill updated successfully",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = UtilityBill.class))),
            @ApiResponse(responseCode = "404", description = "Bill not found for the user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UtilityBill> updateBill(
            @Parameter(description = "ID of the utility bill to update") @PathVariable Long id,
            @RequestBody UtilityBill utilityBill) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<UtilityBill> updated = utilityBillService.updateBill(id, userId, utilityBill);
        return updated.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Mark a utility bill as paid for the current user",
               description = "Changes the status of a specific utility bill to 'PAID'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bill marked as paid successfully"),
            @ApiResponse(responseCode = "404", description = "Bill not found for the user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @PostMapping("/{id}/pay")
    public ResponseEntity<String> payBill(
            @Parameter(description = "ID of the utility bill to mark as paid") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        boolean paid = utilityBillService.payBill(id, userId);
        return paid ? new ResponseEntity<>("Bill marked as paid successfully!", HttpStatus.OK)
                : new ResponseEntity<>("Bill not found for the user or could not be paid.", HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Delete a utility bill for the current user",
               description = "Deletes a specific utility bill by ID, ensuring it belongs to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bill deleted successfully (No Content)"),
            @ApiResponse(responseCode = "404", description = "Bill not found for the user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(
            @Parameter(description = "ID of the utility bill to delete") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        boolean deleted = utilityBillService.deleteBill(id, userId);
        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}