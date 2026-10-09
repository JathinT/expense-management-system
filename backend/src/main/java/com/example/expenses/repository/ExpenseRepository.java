package com.example.expenses.repository;

import com.example.expenses.entity.Expense;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("select coalesce(sum(e.amount), 0) from Expense e")
    BigDecimal calculateTotal();
}
