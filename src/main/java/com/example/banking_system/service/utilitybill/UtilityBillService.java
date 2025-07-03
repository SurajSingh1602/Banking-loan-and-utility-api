package com.example.banking_system.service.utilitybill;

import org.springframework.stereotype.Service;

import com.example.banking_system.model.utilitybill.UtilityBill;
import com.example.banking_system.repository.utilitybill.UtilityBillRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * UtilityBillService contains the business logic for managing utility bills.
 * This service interacts with the UtilityBillRepository to perform operations.
 */
@Service
public class UtilityBillService {

    private final UtilityBillRepository utilityBillRepository;

    public UtilityBillService(UtilityBillRepository utilityBillRepository) {
        this.utilityBillRepository = utilityBillRepository;
    }

    public List<UtilityBill> getAllBillsForUser(Long userId) {
        return utilityBillRepository.findByUserId(userId);
    }

    public Optional<UtilityBill> getBillByIdForUser(Long id, Long userId) {
        List<UtilityBill> bills = utilityBillRepository.findByIdAndUserId(id, userId);
        return bills.isEmpty() ? Optional.empty() : Optional.of(bills.get(0));
    }

    public UtilityBill createBill(UtilityBill utilityBill) {
        if (utilityBill.getStatus() == null || utilityBill.getStatus().isEmpty()) {
            utilityBill.setStatus("UNPAID");
        }
        return utilityBillRepository.save(utilityBill);
    }

    public Optional<UtilityBill> updateBill(Long id, Long userId, UtilityBill updatedBill) {
        Optional<UtilityBill> existingBillOptional = getBillByIdForUser(id, userId);
        if (existingBillOptional.isPresent()) {
            UtilityBill existingBill = existingBillOptional.get();
            existingBill.setBillerName(updatedBill.getBillerName());
            existingBill.setAccountNumber(updatedBill.getAccountNumber());
            existingBill.setAmount(updatedBill.getAmount());
            existingBill.setDueDate(updatedBill.getDueDate());
            existingBill.setStatus(updatedBill.getStatus());
            return Optional.of(utilityBillRepository.save(existingBill));
        }
        return Optional.empty();
    }

    public boolean payBill(Long id, Long userId) {
        Optional<UtilityBill> existingBillOptional = getBillByIdForUser(id, userId);
        if (existingBillOptional.isPresent()) {
            UtilityBill existingBill = existingBillOptional.get();
            existingBill.setStatus("PAID");
            utilityBillRepository.save(existingBill);
            return true;
        }
        return false;
    }

    public boolean deleteBill(Long id, Long userId) {
        Optional<UtilityBill> existingBillOptional = getBillByIdForUser(id, userId);
        if (existingBillOptional.isPresent()) {
            utilityBillRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<UtilityBill> checkAndMarkOverdueBills(Long userId) {
        List<UtilityBill> userBills = utilityBillRepository.findByUserId(userId);
        LocalDate today = LocalDate.now();
        List<UtilityBill> updatedOverdueBills = userBills.stream()
                .filter(bill -> "UNPAID".equals(bill.getStatus()) && bill.getDueDate().isBefore(today))
                .peek(bill -> {
                    bill.setStatus("OVERDUE");
                    utilityBillRepository.save(bill);
                })
                .toList();
        return updatedOverdueBills;
    }
}
