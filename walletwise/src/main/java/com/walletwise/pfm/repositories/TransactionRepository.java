package com.walletwise.pfm.repositories;

import com.walletwise.pfm.entities.Transaction;
import com.walletwise.pfm.entities.TransactionCategory;
import com.walletwise.pfm.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserOrderByDateDesc(User user);

    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(
            User user, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByUserAndCategoryOrderByDateDesc(User user, TransactionCategory category);

    List<Transaction> findByUserAndDateBetweenAndCategoryOrderByDateDesc(
            User user, LocalDate start, LocalDate end, TransactionCategory category);

    boolean existsByCategory(TransactionCategory category);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.date >= :startDate AND t.category.type = 'INCOME'")
    BigDecimal sumIncomeByUserSince(@Param("user") User user, @Param("startDate") LocalDate startDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.date >= :startDate AND t.category.type = 'EXPENSE'")
    BigDecimal sumExpenseByUserSince(@Param("user") User user, @Param("startDate") LocalDate startDate);

    @Query("SELECT t.category.name, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND YEAR(t.date) = :year AND MONTH(t.date) = :month " +
           "AND t.category.type = 'INCOME' GROUP BY t.category.name")
    List<Object[]> sumIncomeByCategory(@Param("user") User user,
                                        @Param("year") int year, @Param("month") int month);

    @Query("SELECT t.category.name, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND YEAR(t.date) = :year AND MONTH(t.date) = :month " +
           "AND t.category.type = 'EXPENSE' GROUP BY t.category.name")
    List<Object[]> sumExpenseByCategory(@Param("user") User user,
                                         @Param("year") int year, @Param("month") int month);

    @Query("SELECT t.category.name, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND YEAR(t.date) = :year " +
           "AND t.category.type = 'INCOME' GROUP BY t.category.name")
    List<Object[]> sumIncomeByCategoryYear(@Param("user") User user, @Param("year") int year);

    @Query("SELECT t.category.name, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND YEAR(t.date) = :year " +
           "AND t.category.type = 'EXPENSE' GROUP BY t.category.name")
    List<Object[]> sumExpenseByCategoryYear(@Param("user") User user, @Param("year") int year);
}
