package com.walletwise.pfm.services;

import com.walletwise.pfm.dto.request.CategoryRequest;
import com.walletwise.pfm.dto.response.CategoryResponse;
import com.walletwise.pfm.entities.TransactionCategory;
import com.walletwise.pfm.entities.User;
import com.walletwise.pfm.exception.GlobalExceptionHandler.*;
import com.walletwise.pfm.repositories.TransactionCategoryRepository;
import com.walletwise.pfm.repositories.TransactionRepository;
import com.walletwise.pfm.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock TransactionCategoryRepository categoryRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock UserRepository userRepository;
    @InjectMocks CategoryService categoryService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("user@test.com");
        when(userRepository.findByUsername("user@test.com")).thenReturn(Optional.of(user));
    }

    @Test
    void createCategory_success() {
        when(categoryRepository.existsByNameAndUserIsNull("MyBudget")).thenReturn(false);
        when(categoryRepository.existsByNameAndUser("MyBudget", user)).thenReturn(false);
        TransactionCategory saved = new TransactionCategory();
        saved.setName("MyBudget");
        saved.setType(TransactionCategory.CategoryType.EXPENSE);
        saved.setCustom(true);
        when(categoryRepository.save(any())).thenReturn(saved);

        CategoryRequest req = new CategoryRequest();
        req.setName("MyBudget");
        req.setType("EXPENSE");

        CategoryResponse resp = categoryService.createCategory("user@test.com", req);
        assertThat(resp.getName()).isEqualTo("MyBudget");
        assertThat(resp.isCustom()).isTrue();
    }

    @Test
    void createCategory_conflictOnDefault() {
        when(categoryRepository.existsByNameAndUserIsNull("Salary")).thenReturn(true);

        CategoryRequest req = new CategoryRequest();
        req.setName("Salary");
        req.setType("INCOME");

        assertThatThrownBy(() -> categoryService.createCategory("user@test.com", req))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deleteCategory_defaultThrowsForbidden() {
        when(categoryRepository.existsByNameAndUserIsNull("Salary")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory("user@test.com", "Salary"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteCategory_inUseThrowsBadRequest() {
        when(categoryRepository.existsByNameAndUserIsNull("CustomCat")).thenReturn(false);
        TransactionCategory cat = new TransactionCategory();
        cat.setName("CustomCat");
        when(categoryRepository.findByNameAndUser("CustomCat", user)).thenReturn(Optional.of(cat));
        when(transactionRepository.existsByCategory(cat)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory("user@test.com", "CustomCat"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getAllCategories_includesDefaultAndCustom() {
        TransactionCategory def = new TransactionCategory();
        def.setName("Salary");
        def.setType(TransactionCategory.CategoryType.INCOME);
        def.setCustom(false);

        when(categoryRepository.findByUserIsNullOrUser(user)).thenReturn(List.of(def));

        List<CategoryResponse> cats = categoryService.getAllCategories("user@test.com");
        assertThat(cats).hasSize(1);
        assertThat(cats.get(0).getName()).isEqualTo("Salary");
    }
}
