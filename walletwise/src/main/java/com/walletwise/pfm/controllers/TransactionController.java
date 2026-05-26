package com.walletwise.pfm.controllers;

import com.walletwise.pfm.dto.request.TransactionRequest;
import com.walletwise.pfm.dto.request.UpdateTransactionRequest;
import com.walletwise.pfm.dto.response.TransactionResponse;
import com.walletwise.pfm.services.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /** GET /api/transactions */
    @GetMapping
    public ResponseEntity<Map<String, List<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String categoryId) {

        List<TransactionResponse> txns = transactionService.getTransactions(
                userDetails.getUsername(), startDate, endDate, categoryId);
        return ResponseEntity.ok(Map.of("transactions", txns));
    }

    /** POST /api/transactions */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransactionRequest req) {

        TransactionResponse txn = transactionService.createTransaction(userDetails.getUsername(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(txn);
    }

    /** PUT /api/transactions/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest req) {

        return ResponseEntity.ok(transactionService.updateTransaction(userDetails.getUsername(), id, req));
    }

    /** DELETE /api/transactions/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        transactionService.deleteTransaction(userDetails.getUsername(), id);
        return ResponseEntity.ok(Map.of("message", "Transaction deleted successfully"));
    }
}
