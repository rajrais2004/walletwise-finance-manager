package com.walletwise.pfm.services;

import com.walletwise.pfm.dto.request.TransactionRequest;
import com.walletwise.pfm.dto.request.UpdateTransactionRequest;
import com.walletwise.pfm.dto.response.TransactionResponse;
import com.walletwise.pfm.entities.Transaction;
import com.walletwise.pfm.entities.TransactionCategory;
import com.walletwise.pfm.entities.User;
import com.walletwise.pfm.exception.GlobalExceptionHandler.*;
import com.walletwise.pfm.repositories.TransactionRepository;
import com.walletwise.pfm.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock UserRepository userRepository;
    @Mock CategoryService categoryService;
    @InjectMocks TransactionService transactionService;

    private User user;
    private TransactionCategory salaryCategory;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("test@test.com");

        salaryCategory = new TransactionCategory();
        salaryCategory.setName("Salary");
        salaryCategory.setType(TransactionCategory.CategoryType.INCOME);
        salaryCategory.setCustom(false);
    }

    @Test
    void createTransaction_success() {
        when(userRepository.findByUsername("test@test.com")).thenReturn(Optional.of(user));
        when(categoryService.resolveCategory("Salary", user)).thenReturn(salaryCategory);

        Transaction saved = new Transaction();
        saved.setAmount(BigDecimal.valueOf(5000));
        saved.setDate(LocalDate.now().minusDays(1));
        saved.setCategory(salaryCategory);
        saved.setUser(user);
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionRequest req = new TransactionRequest();
        req.setAmount(BigDecimal.valueOf(5000));
        req.setDate(LocalDate.now().minusDays(1));
        req.setCategory("Salary");

        TransactionResponse resp = transactionService.createTransaction("test@test.com", req);

        assertThat(resp.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(resp.getType()).isEqualTo("INCOME");
    }

    @Test
    void createTransaction_futureDateThrows() {
        when(userRepository.findByUsername("test@test.com")).thenReturn(Optional.of(user));
        when(categoryService.resolveCategory("Salary", user)).thenReturn(salaryCategory);

        TransactionRequest req = new TransactionRequest();
        req.setAmount(BigDecimal.valueOf(100));
        req.setDate(LocalDate.now().plusDays(1));
        req.setCategory("Salary");

        assertThatThrownBy(() -> transactionService.createTransaction("test@test.com", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("future");
    }

    @Test
    void getTransactions_returnsAll() {
        when(userRepository.findByUsername("test@test.com")).thenReturn(Optional.of(user));

        Transaction t = new Transaction();
        t.setAmount(BigDecimal.valueOf(200));
        t.setDate(LocalDate.now());
        t.setCategory(salaryCategory);
        t.setUser(user);
        when(transactionRepository.findByUserOrderByDateDesc(user)).thenReturn(List.of(t));

        List<TransactionResponse> result = transactionService.getTransactions("test@test.com", null, null, null);
        assertThat(result).hasSize(1);
    }

    @Test
    void deleteTransaction_notOwnedThrows() {
        User other = new User();
        other.setUsername("other@test.com");

        Transaction t = new Transaction();
        t.setUser(other);
        t.setCategory(salaryCategory);

        when(userRepository.findByUsername("test@test.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> transactionService.deleteTransaction("test@test.com", 1L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteTransaction_notFoundThrows() {
        when(userRepository.findByUsername("test@test.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction("test@test.com", 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateTransaction_changesAmountAndDescription() {
        Transaction existing = new Transaction();
        existing.setUser(user);
        existing.setAmount(BigDecimal.valueOf(100));
        existing.setDate(LocalDate.now());
        existing.setCategory(salaryCategory);
        existing.setDescription("old");

        when(userRepository.findByUsername("test@test.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UpdateTransactionRequest req = new UpdateTransactionRequest();
        req.setAmount(BigDecimal.valueOf(999));
        req.setDescription("updated");

        TransactionResponse resp = transactionService.updateTransaction("test@test.com", 1L, req);
        assertThat(resp.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(999));
        assertThat(resp.getDescription()).isEqualTo("updated");
    }
}
