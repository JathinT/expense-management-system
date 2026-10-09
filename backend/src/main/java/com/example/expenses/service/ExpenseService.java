package com.example.expenses.service;

import com.example.expenses.dto.ExpenseRequest;
import com.example.expenses.entity.Expense;
import com.example.expenses.repository.ExpenseRepository;
import java.math.BigDecimal;
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
        return expenseRepository.calculateTotal();
    }
}
