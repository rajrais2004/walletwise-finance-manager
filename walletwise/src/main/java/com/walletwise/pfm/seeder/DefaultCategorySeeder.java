package com.walletwise.pfm.seeder;

import com.walletwise.pfm.entities.TransactionCategory;
import com.walletwise.pfm.repositories.TransactionCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DefaultCategorySeeder implements CommandLineRunner {

    private final TransactionCategoryRepository categoryRepository;

    public DefaultCategorySeeder(TransactionCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        seedIfAbsent("Salary", TransactionCategory.CategoryType.INCOME);
        seedIfAbsent("Food", TransactionCategory.CategoryType.EXPENSE);
        seedIfAbsent("Rent", TransactionCategory.CategoryType.EXPENSE);
        seedIfAbsent("Transportation", TransactionCategory.CategoryType.EXPENSE);
        seedIfAbsent("Entertainment", TransactionCategory.CategoryType.EXPENSE);
        seedIfAbsent("Healthcare", TransactionCategory.CategoryType.EXPENSE);
        seedIfAbsent("Utilities", TransactionCategory.CategoryType.EXPENSE);
    }

    private void seedIfAbsent(String name, TransactionCategory.CategoryType type) {
        if (!categoryRepository.existsByNameAndUserIsNull(name)) {
            TransactionCategory cat = new TransactionCategory();
            cat.setName(name);
            cat.setType(type);
            cat.setCustom(false);
            cat.setUser(null);
            categoryRepository.save(cat);
        }
    }
}
