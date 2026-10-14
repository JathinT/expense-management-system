package com.example.expenses.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.expenses.entity.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    private Expense foodStart;
    private Expense foodMid;
    private Expense foodEnd;
    private Expense foodOutside;
    private Expense utilities;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();

        foodStart = new Expense(new BigDecimal("10.00"), "Food", "Start boundary", LocalDate.of(2026, 10, 1));
        foodMid = new Expense(new BigDecimal("5.50"), "Food", "Middle", LocalDate.of(2026, 10, 15));
        foodEnd = new Expense(new BigDecimal("3.25"), "Food", "End boundary", LocalDate.of(2026, 10, 31));
        foodOutside = new Expense(new BigDecimal("2.00"), "Food", "Outside before", LocalDate.of(2026, 9, 30));
        utilities = new Expense(new BigDecimal("7.00"), "Utilities", "Different category", LocalDate.of(2026, 10, 15));

        expenseRepository.saveAll(List.of(foodStart, foodMid, foodEnd, foodOutside, utilities));
    }

    @Test
    void returnsAllWhenNoFilters() {
        List<Expense> all = expenseRepository.findByOptionalFilters(null, null, null);
        assertEquals(5, all.size(), "Should return all saved expenses when no filters provided");
    }

    @Test
    void filterByCategoryOnly() {
        List<Expense> results = expenseRepository.findByOptionalFilters("food", null, null);
        // 4 food expenses were saved
        assertEquals(4, results.size(), "Category filter should be case-insensitive and return only matching category");
        assertTrue(results.stream().allMatch(e -> e.getCategory().equalsIgnoreCase("food")));
    }

    @Test
    void filterByDateRangeOnly_includesBoundaries() {
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);
        List<Expense> results = expenseRepository.findByOptionalFilters(null, start, end);
        // Should include foodStart, foodMid, foodEnd, and utilities => 4
        assertEquals(4, results.size(), "Date range filter should include start and end date boundaries");
        assertTrue(results.stream().anyMatch(e -> e.getExpenseDate().isEqual(start)), "Should include start boundary expense");
        assertTrue(results.stream().anyMatch(e -> e.getExpenseDate().isEqual(end)), "Should include end boundary expense");
    }

    @Test
    void combinedCategoryAndDateFilters_workAndIncludeBoundaries() {
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);
        List<Expense> results = expenseRepository.findByOptionalFilters("Food", start, end);
        // Should include only the three Food expenses within the date range: start, mid, end
        assertEquals(3, results.size(), "Combined category and date filters should restrict results correctly");
        assertTrue(results.stream().noneMatch(e -> e.getCategory().equalsIgnoreCase("Utilities")));

        BigDecimal total = expenseRepository.calculateTotalWithFilters("Food", start, end);
        BigDecimal expected = new BigDecimal("18.75"); // 10.00 + 5.50 + 3.25
        assertEquals(0, total.compareTo(expected), "Total with filters should equal sum of matching expense amounts");
    }
}
