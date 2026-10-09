package com.example.expenses.dto;

import com.example.expenses.entity.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(Long id, BigDecimal amount, String category, String description, LocalDate expenseDate) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(expense.getId(), expense.getAmount(), expense.getCategory(),
                expense.getDescription(), expense.getExpenseDate());
    }
}
