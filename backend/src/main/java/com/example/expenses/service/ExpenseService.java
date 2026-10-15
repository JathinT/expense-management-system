package com.example.expenses.service;

import com.example.expenses.dto.ExpenseRequest;
import com.example.expenses.entity.Expense;
import com.example.expenses.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public Expense create(ExpenseRequest request) {
        Expense expense = new Expense(request.amount(), request.category().trim(),
                request.description() == null ? null : request.description().trim(), request.expenseDate());
        return expenseRepository.save(expense);
    }

    @Transactional(readOnly = true)
    public List<Expense> findAll() {
        return expenseRepository.findAll();
    }

    /**
     * Find expenses with optional filters. Any parameter may be null to indicate no filtering on that field.
     */
    @Transactional(readOnly = true)
    public List<Expense> findAll(String category, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByOptionalFilters(category, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Expense findById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
    }

    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotal() {
        // Some implementations may provide a calculateTotal() custom query; if not, use repository.calculateTotalWithFilters with nulls.
        try {
            return expenseRepository.calculateTotalWithFilters(null, null, null);
        } catch (Exception e) {
            // Fall back to summing in Java if repository method not available for any reason
            return expenseRepository.findAll().stream()
                    .map(Expense::getAmount)
                    .filter(a -> a != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    /**
     * Calculate total with optional filters. Any parameter may be null.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotal(String category, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.calculateTotalWithFilters(category, startDate, endDate);
    }
}