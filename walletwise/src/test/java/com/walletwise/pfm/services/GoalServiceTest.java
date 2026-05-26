package com.walletwise.pfm.services;

import com.walletwise.pfm.dto.request.GoalRequest;
import com.walletwise.pfm.dto.request.UpdateGoalRequest;
import com.walletwise.pfm.dto.response.GoalResponse;
import com.walletwise.pfm.entities.SavingsGoal;
import com.walletwise.pfm.entities.User;
import com.walletwise.pfm.exception.GlobalExceptionHandler.*;
import com.walletwise.pfm.repositories.SavingsGoalRepository;
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
class GoalServiceTest {

    @Mock SavingsGoalRepository goalRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock UserRepository userRepository;
    @InjectMocks GoalService goalService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("user@test.com");
        when(userRepository.findByUsername("user@test.com")).thenReturn(Optional.of(user));
    }

    @Test
    void createGoal_success() {
        LocalDate future = LocalDate.now().plusMonths(6);

        SavingsGoal saved = new SavingsGoal();
        saved.setGoalName("Emergency Fund");
        saved.setTargetAmount(BigDecimal.valueOf(5000));
        saved.setTargetDate(future);
        saved.setStartDate(LocalDate.now());
        saved.setUser(user);

        when(goalRepository.save(any())).thenReturn(saved);
        when(transactionRepository.sumIncomeByUserSince(eq(user), any())).thenReturn(BigDecimal.valueOf(1000));
        when(transactionRepository.sumExpenseByUserSince(eq(user), any())).thenReturn(BigDecimal.valueOf(200));

        GoalRequest req = new GoalRequest();
        req.setGoalName("Emergency Fund");
        req.setTargetAmount(BigDecimal.valueOf(5000));
        req.setTargetDate(future);

        GoalResponse resp = goalService.createGoal("user@test.com", req);
        assertThat(resp.getGoalName()).isEqualTo("Emergency Fund");
        assertThat(resp.getCurrentProgress()).isEqualByComparingTo(BigDecimal.valueOf(800));
    }

    @Test
    void createGoal_pastTargetDateThrows() {
        GoalRequest req = new GoalRequest();
        req.setGoalName("Bad Goal");
        req.setTargetAmount(BigDecimal.valueOf(100));
        req.setTargetDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> goalService.createGoal("user@test.com", req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteGoal_notFoundThrows() {
        when(goalRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.deleteGoal("user@test.com", 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllGoals_returnsEmptyList() {
        when(goalRepository.findByUser(user)).thenReturn(List.of());
        List<GoalResponse> goals = goalService.getAllGoals("user@test.com");
        assertThat(goals).isEmpty();
    }

    @Test
    void updateGoal_updatesBothFields() {
        LocalDate future = LocalDate.now().plusMonths(3);
        SavingsGoal existing = new SavingsGoal();
        existing.setGoalName("Fund");
        existing.setTargetAmount(BigDecimal.valueOf(1000));
        existing.setTargetDate(future);
        existing.setStartDate(LocalDate.now().minusMonths(1));
        existing.setUser(user);

        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));
        when(goalRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.sumIncomeByUserSince(any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumExpenseByUserSince(any(), any())).thenReturn(BigDecimal.ZERO);

        UpdateGoalRequest req = new UpdateGoalRequest();
        req.setTargetAmount(BigDecimal.valueOf(2000));
        req.setTargetDate(future.plusMonths(1));

        GoalResponse resp = goalService.updateGoal("user@test.com", 1L, req);
        assertThat(resp.getTargetAmount()).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }
}
