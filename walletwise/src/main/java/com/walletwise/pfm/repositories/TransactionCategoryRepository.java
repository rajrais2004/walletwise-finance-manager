package com.walletwise.pfm.repositories;

import com.walletwise.pfm.entities.TransactionCategory;
import com.walletwise.pfm.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {
    List<TransactionCategory> findByUserIsNullOrUser(User user);
    Optional<TransactionCategory> findByNameAndUserIsNull(String name);
    Optional<TransactionCategory> findByNameAndUser(String name, User user);
    Optional<TransactionCategory> findByIdAndUser(Long id, User user);
    Optional<TransactionCategory> findByIdAndUserIsNull(Long id);
    boolean existsByNameAndUser(String name, User user);
    boolean existsByNameAndUserIsNull(String name);
}
