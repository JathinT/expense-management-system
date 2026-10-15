package com.example.expenses.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.expenses.dto.ExpenseRequest;
import com.example.expenses.entity.Expense;
import com.example.expenses.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private Expense expense;

    @BeforeEach
    void setUp() {
        expense = new Expense(new BigDecimal("12.50"), "Food", "Lunch", LocalDate.of(2026, 10, 1));
    }

    @Test
    void createsExpense() {
        ExpenseRequest request = new ExpenseRequest(expense.getAmount(), expense.getCategory(),
                expense.getDescription(), expense.getExpenseDate());
        when(expenseRepository.save(org.mockito.ArgumentMatchers.any(Expense.class))).thenReturn(expense);

        Expense created = expenseService.create(request);

        assertEquals(expense, created);
        verify(expenseRepository).save(org.mockito.ArgumentMatchers.any(Expense.class));
    }

    @Test
    void retrievesExpenses() {
        when(expenseRepository.findAll()).thenReturn(List.of(expense));

        assertEquals(List.of(expense), expenseService.findAll());
    }

    @Test
    void retrievesExpensesWithFilters() {
        when(expenseRepository.findByOptionalFilters("Food", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(expense));

        List<Expense> results = expenseService.findAll("Food", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        assertEquals(List.of(expense), results);
    }

    @Test
    void calculatesTotal() {
        when(expenseRepository.calculateTotalWithFilters(null, null, null)).thenReturn(new BigDecimal("12.50"));

        assertEquals(new BigDecimal("12.50"), expenseService.calculateTotal());
    }

    @Test
    void calculatesTotalWithFilters() {
        when(expenseRepository.calculateTotalWithFilters("Food", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(new BigDecimal("12.50"));

        assertEquals(new BigDecimal("12.50"), expenseService.calculateTotal("Food", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)));
    }

    @Test
    void deletesExistingExpense() {
        when(expenseRepository.existsById(1L)).thenReturn(true);

        expenseService.delete(1L);

        verify(expenseRepository).deleteById(1L);
    }

    @Test
    void rejectsInvalidAmount() {
        ExpenseRequest invalidRequest = new ExpenseRequest(BigDecimal.ZERO, "Food", null, LocalDate.now());
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            var violations = validator.validate(invalidRequest);
            assertTrue(violations.stream().anyMatch(violation ->
                    violation.getPropertyPath().toString().equals("amount")));
        }
    }

    @Test
    void returnsNotFoundForMissingExpense() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> expenseService.findById(99L));
    }
}