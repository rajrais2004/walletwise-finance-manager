package com.walletwise.pfm.services;

import com.walletwise.pfm.dto.request.CategoryRequest;
import com.walletwise.pfm.dto.response.CategoryResponse;
import com.walletwise.pfm.entities.TransactionCategory;
import com.walletwise.pfm.entities.User;
import com.walletwise.pfm.exception.GlobalExceptionHandler.*;
import com.walletwise.pfm.repositories.TransactionCategoryRepository;
import com.walletwise.pfm.repositories.TransactionRepository;
import com.walletwise.pfm.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final TransactionCategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public CategoryService(TransactionCategoryRepository categoryRepository,
                           TransactionRepository transactionRepository,
                           UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Returns all default categories plus the user's custom categories.
     */
    public List<CategoryResponse> getAllCategories(String username) {
        User user = getUser(username);
        return categoryRepository.findByUserIsNullOrUser(user).stream()
                .map(c -> new CategoryResponse(c.getName(), c.getType().name(), c.isCustom()))
                .collect(Collectors.toList());
    }

    /**
     * Creates a new custom category for the authenticated user.
     */
    public CategoryResponse createCategory(String username, CategoryRequest req) {
        User user = getUser(username);

        boolean defaultExists = categoryRepository.existsByNameAndUserIsNull(req.getName());
        boolean customExists = categoryRepository.existsByNameAndUser(req.getName(), user);
        if (defaultExists || customExists) {
            throw new ConflictException("Category already exists: " + req.getName());
        }

        TransactionCategory cat = new TransactionCategory();
        cat.setName(req.getName());
        cat.setType(TransactionCategory.CategoryType.valueOf(req.getType()));
        cat.setCustom(true);
        cat.setUser(user);
        categoryRepository.save(cat);

        return new CategoryResponse(cat.getName(), cat.getType().name(), true);
    }

    /**
     * Deletes a custom category by name. Cannot delete default categories.
     */
    public void deleteCategory(String username, String name) {
        User user = getUser(username);

        // Check if it's a default category
        if (categoryRepository.existsByNameAndUserIsNull(name)) {
            throw new ForbiddenException("Cannot delete default category: " + name);
        }

        TransactionCategory cat = categoryRepository.findByNameAndUser(name, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + name));

        if (transactionRepository.existsByCategory(cat)) {
            throw new BadRequestException("Category is in use and cannot be deleted");
        }

        categoryRepository.delete(cat);
    }


    /**
     * Resolves a category from either the assignment request body category name
     * or the optional query parameter categoryId. Numeric values are treated as IDs;
     * non-numeric values are treated as names so older scripts still work.
     */
    public TransactionCategory resolveCategoryIdentifier(String categoryIdOrName, User user) {
        if (categoryIdOrName == null || categoryIdOrName.isBlank()) {
            throw new BadRequestException("Category is required");
        }
        try {
            Long id = Long.parseLong(categoryIdOrName);
            return categoryRepository.findByIdAndUser(id, user)
                    .or(() -> categoryRepository.findByIdAndUserIsNull(id))
                    .orElseThrow(() -> new BadRequestException("Category not found: " + categoryIdOrName));
        } catch (NumberFormatException ignored) {
            return resolveCategory(categoryIdOrName, user);
        }
    }

    /**
     * Resolves a category by name for the given user (checks default + custom).
     */
    public TransactionCategory resolveCategory(String name, User user) {
        return categoryRepository.findByNameAndUser(name, user)
                .or(() -> categoryRepository.findByNameAndUserIsNull(name))
                .orElseThrow(() -> new BadRequestException("Category not found: " + name));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
