package com.walletwise.pfm.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InfoController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "app", "WalletWise"));
    }

    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        return ResponseEntity.ok(Map.of(
                "app", "WalletWise Personal Finance Manager",
                "status", "running",
                "auth", "/api/auth/register, /api/auth/login, /api/auth/logout",
                "resources", "/api/transactions, /api/categories, /api/goals, /api/reports"
        ));
    }
}
