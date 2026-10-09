package com.example.expenses.controller;

import com.example.expenses.dto.ExpenseRequest;
import com.example.expenses.dto.ExpenseResponse;
import com.example.expenses.dto.ExpenseTotalResponse;
import com.example.expenses.service.ExpenseService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = ExpenseResponse.from(expenseService.create(request));
        return ResponseEntity.created(URI.create("/api/expenses/" + response.id())).body(response);
    }

    @GetMapping
    public List<ExpenseResponse> findAll() {
        return expenseService.findAll().stream().map(ExpenseResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ExpenseResponse findById(@PathVariable Long id) {
        return ExpenseResponse.from(expenseService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total")
    public ExpenseTotalResponse total() {
        return new ExpenseTotalResponse(expenseService.calculateTotal());
    }
}
