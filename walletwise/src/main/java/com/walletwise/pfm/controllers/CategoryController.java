package com.walletwise.pfm.controllers;

import com.walletwise.pfm.dto.request.CategoryRequest;
import com.walletwise.pfm.dto.response.CategoryResponse;
import com.walletwise.pfm.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** GET /api/categories */
    @GetMapping
    public ResponseEntity<Map<String, List<CategoryResponse>>> getCategories(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CategoryResponse> cats = categoryService.getAllCategories(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("categories", cats));
    }

    /** POST /api/categories */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CategoryRequest req) {
        CategoryResponse cat = categoryService.createCategory(userDetails.getUsername(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(cat);
    }

    /** DELETE /api/categories/{name} */
    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, String>> deleteCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String name) {
        categoryService.deleteCategory(userDetails.getUsername(), name);
        return ResponseEntity.ok(Map.of("message", "Category deleted successfully"));
    }
}
