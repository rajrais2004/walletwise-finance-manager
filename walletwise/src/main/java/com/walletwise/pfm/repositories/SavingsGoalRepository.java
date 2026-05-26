package com.walletwise.pfm.repositories;

import com.walletwise.pfm.entities.SavingsGoal;
import com.walletwise.pfm.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findByUser(User user);
    Optional<SavingsGoal> findByIdAndUser(Long id, User user);
}
