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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalService {

    private final SavingsGoalRepository goalRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public GoalService(SavingsGoalRepository goalRepository,
                       TransactionRepository transactionRepository,
                       UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public GoalResponse createGoal(String username, GoalRequest req) {
        User user = getUser(username);

        if (!req.getTargetDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be in the future");
        }

        SavingsGoal goal = new SavingsGoal();
        goal.setUser(user);
        goal.setGoalName(req.getGoalName());
        goal.setTargetAmount(req.getTargetAmount());
        goal.setTargetDate(req.getTargetDate());
        goal.setStartDate(req.getStartDate() != null ? req.getStartDate() : LocalDate.now());

        return toResponse(goalRepository.save(goal), user);
    }

    public List<GoalResponse> getAllGoals(String username) {
        User user = getUser(username);
        return goalRepository.findByUser(user).stream()
                .map(g -> toResponse(g, user))
                .collect(Collectors.toList());
    }

    public GoalResponse getGoal(String username, Long id) {
        User user = getUser(username);
        SavingsGoal goal = goalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + id));
        return toResponse(goal, user);
    }

    public GoalResponse updateGoal(String username, Long id, UpdateGoalRequest req) {
        User user = getUser(username);
        SavingsGoal goal = goalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + id));

        if (req.getTargetDate() != null) {
            if (!req.getTargetDate().isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be in the future");
            }
            goal.setTargetDate(req.getTargetDate());
        }
        if (req.getTargetAmount() != null) {
            goal.setTargetAmount(req.getTargetAmount());
        }

        return toResponse(goalRepository.save(goal), user);
    }

    public void deleteGoal(String username, Long id) {
        User user = getUser(username);
        SavingsGoal goal = goalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + id));
        goalRepository.delete(goal);
    }

    /**
     * Progress = total income - total expenses since goal start date.
     */
    private GoalResponse toResponse(SavingsGoal goal, User user) {
        BigDecimal income = transactionRepository.sumIncomeByUserSince(user, goal.getStartDate());
        BigDecimal expense = transactionRepository.sumExpenseByUserSince(user, goal.getStartDate());
        BigDecimal progress = income.subtract(expense);

        BigDecimal remaining = goal.getTargetAmount().subtract(progress);
        double pct = 0.0;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            pct = progress.divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        GoalResponse r = new GoalResponse();
        r.setId(goal.getId());
        r.setGoalName(goal.getGoalName());
        r.setTargetAmount(goal.getTargetAmount());
        r.setTargetDate(goal.getTargetDate());
        r.setStartDate(goal.getStartDate());
        r.setCurrentProgress(progress.setScale(2, RoundingMode.HALF_UP));
        r.setProgressPercentage(pct);
        r.setRemainingAmount(remaining.setScale(2, RoundingMode.HALF_UP));
        return r;
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
