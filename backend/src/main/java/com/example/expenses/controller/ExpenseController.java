package com.example.expenses.controller;

import com.example.expenses.dto.ExpenseRequest;
import com.example.expenses.dto.ExpenseResponse;
import com.example.expenses.dto.ExpenseTotalResponse;
import com.example.expenses.entity.Expense;
import com.example.expenses.service.ExpenseService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody ExpenseRequest request) {
        Expense expense = expenseService.create(request);
        return ExpenseResponse.from(expense);
    }

    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<Expense> expenses = expenseService.findAll(category, startDate, endDate);
        return expenses.stream().map(ExpenseResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/total")
    public ExpenseTotalResponse total(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        BigDecimal total = expenseService.calculateTotal(category, startDate, endDate);
        return new ExpenseTotalResponse(total);
    }

    @GetMapping("/{id}")
    public ExpenseResponse getOne(@PathVariable Long id) {
        return ExpenseResponse.from(expenseService.findById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        expenseService.delete(id);
    }
}