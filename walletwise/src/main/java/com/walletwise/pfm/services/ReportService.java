package com.walletwise.pfm.services;

import com.walletwise.pfm.entities.User;
import com.walletwise.pfm.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.walletwise.pfm.repositories.TransactionRepository;
import com.walletwise.pfm.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public ReportService(TransactionRepository transactionRepository,
                         UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Monthly report: income by category, expenses by category, net savings.
     */
    public Map<String, Object> monthlyReport(String username, int year, int month) {
        User user = getUser(username);

        Map<String, BigDecimal> income = toMap(
                transactionRepository.sumIncomeByCategory(user, year, month));
        Map<String, BigDecimal> expenses = toMap(
                transactionRepository.sumExpenseByCategory(user, year, month));

        BigDecimal totalIncome = income.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = expenses.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpense).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("month", month);
        report.put("year", year);
        report.put("totalIncome", income);
        report.put("totalExpenses", expenses);
        report.put("netSavings", netSavings);
        return report;
    }

    /**
     * Yearly report: aggregated income and expenses by category, net savings.
     */
    public Map<String, Object> yearlyReport(String username, int year) {
        User user = getUser(username);

        Map<String, BigDecimal> income = toMap(
                transactionRepository.sumIncomeByCategoryYear(user, year));
        Map<String, BigDecimal> expenses = toMap(
                transactionRepository.sumExpenseByCategoryYear(user, year));

        BigDecimal totalIncome = income.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = expenses.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpense).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("year", year);
        report.put("totalIncome", income);
        report.put("totalExpenses", expenses);
        report.put("netSavings", netSavings);
        return report;
    }

    private Map<String, BigDecimal> toMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String name = (String) row[0];
            BigDecimal val = ((BigDecimal) row[1]).setScale(2, RoundingMode.HALF_UP);
            map.put(name, val);
        }
        return map;
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
