package com.example.banking_system.repository.utilitybill;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.banking_system.model.utilitybill.UtilityBill;

import java.util.List;

/**
 * UtilityBillRepository interface for performing CRUD operations on UtilityBill entities.
 * Extends JpaRepository to get standard JPA functionalities.
 */
@Repository
public interface UtilityBillRepository extends JpaRepository<UtilityBill, Long> {

    /**
     * Finds a list of UtilityBills by the associated userId.
     * Spring Data JPA will automatically generate the query based on the method name.
     *
     * @param userId The ID of the user whose bills are to be retrieved.
     * @return A list of UtilityBill objects belonging to the specified user.
     */
    List<UtilityBill> findByUserId(Long userId);

    /**
     * Finds a UtilityBill by its ID and the associated userId.
     * This ensures that users can only access their own bills.
     *
     * @param id The ID of the utility bill.
     * @param userId The ID of the user who owns the bill.
     * @return An Optional containing the UtilityBill if found and belongs to the user, otherwise empty.
     */
    List<UtilityBill> findByIdAndUserId(Long id, Long userId);
}