package com.example.expenses.repository;

import com.example.expenses.entity.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE (:category IS NULL OR LOWER(e.category) = LOWER(:category)) "
            + "AND (:startDate IS NULL OR e.expenseDate >= :startDate) "
            + "AND (:endDate IS NULL OR e.expenseDate <= :endDate) "
            + "ORDER BY e.expenseDate DESC")
    List<Expense> findByOptionalFilters(@Param("category") String category,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE (:category IS NULL OR LOWER(e.category) = LOWER(:category)) "
            + "AND (:startDate IS NULL OR e.expenseDate >= :startDate) "
            + "AND (:endDate IS NULL OR e.expenseDate <= :endDate)")
    BigDecimal calculateTotalWithFilters(@Param("category") String category,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
}
