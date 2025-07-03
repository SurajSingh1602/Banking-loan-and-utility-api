package com.example.banking_system.model.utilitybill;



import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

/**
 * UtilityBill entity representing a utility bill in the system.
 * This class is mapped to the 'utility_bills' table in the database.
 */
@Entity
@Table(name = "utility_bills")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilityBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String billerName;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate dueDate;

    private String status;

    @Column(nullable = false)
    private Long userId;

    // You can add more fields as per your requirements, e.g., bill type, payment date, etc.
}