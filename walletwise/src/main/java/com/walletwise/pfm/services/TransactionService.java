package com.walletwise.pfm.services;

import com.walletwise.pfm.dto.request.TransactionRequest;
import com.walletwise.pfm.dto.request.UpdateTransactionRequest;
import com.walletwise.pfm.dto.response.TransactionResponse;
import com.walletwise.pfm.entities.Transaction;
import com.walletwise.pfm.entities.TransactionCategory;
import com.walletwise.pfm.entities.User;
import com.walletwise.pfm.exception.GlobalExceptionHandler.*;
import com.walletwise.pfm.repositories.TransactionRepository;
import com.walletwise.pfm.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository,
                              UserRepository userRepository,
                              CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
    }

    /**
     * Returns transactions for the user, optionally filtered by date range and/or categoryId.
     */
    public List<TransactionResponse> getTransactions(String username,
                                                      LocalDate startDate,
                                                      LocalDate endDate,
                                                      String categoryIdOrName) {
        User user = getUser(username);
        List<Transaction> txns;

        if (startDate != null && endDate != null && categoryIdOrName != null) {
            TransactionCategory cat = categoryService.resolveCategoryIdentifier(categoryIdOrName, user);
            txns = transactionRepository.findByUserAndDateBetweenAndCategoryOrderByDateDesc(
                    user, startDate, endDate, cat);
        } else if (startDate != null && endDate != null) {
            txns = transactionRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate);
        } else if (categoryIdOrName != null) {
            TransactionCategory cat = categoryService.resolveCategoryIdentifier(categoryIdOrName, user);
            txns = transactionRepository.findByUserAndCategoryOrderByDateDesc(user, cat);
        } else {
            txns = transactionRepository.findByUserOrderByDateDesc(user);
        }

        return txns.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Creates a new transaction for the authenticated user.
     */
    public TransactionResponse createTransaction(String username, TransactionRequest req) {
        User user = getUser(username);

        if (req.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be in the future");
        }

        TransactionCategory cat = categoryService.resolveCategory(req.getCategory(), user);

        Transaction txn = new Transaction();
        txn.setUser(user);
        txn.setCategory(cat);
        txn.setAmount(req.getAmount());
        txn.setDate(req.getDate());
        txn.setDescription(req.getDescription());
        txn.setNotes(req.getNotes());
        txn.setRecurring(req.isRecurring());
        if (req.getRecurrenceType() != null && !req.getRecurrenceType().isBlank()) {
            try {
                txn.setRecurrenceType(Transaction.RecurrenceType.valueOf(req.getRecurrenceType().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("recurrenceType must be WEEKLY or MONTHLY");
            }
        }

        return toResponse(transactionRepository.save(txn));
    }

    /**
     * Updates amount, category, description, and notes. Date is immutable per spec.
     */
    public TransactionResponse updateTransaction(String username, Long id, UpdateTransactionRequest req) {
        User user = getUser(username);
        Transaction txn = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));

        if (!txn.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }

        if (req.getAmount() != null) txn.setAmount(req.getAmount());
        if (req.getDescription() != null) txn.setDescription(req.getDescription());
        if (req.getNotes() != null) txn.setNotes(req.getNotes());
        if (req.getCategory() != null) {
            txn.setCategory(categoryService.resolveCategory(req.getCategory(), user));
        }

        return toResponse(transactionRepository.save(txn));
    }

    /**
     * Deletes a transaction by id. The user must own the transaction.
     */
    public void deleteTransaction(String username, Long id) {
        User user = getUser(username);
        Transaction txn = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));

        if (!txn.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }

        transactionRepository.delete(txn);
    }

    public TransactionResponse toResponse(Transaction t) {
        TransactionResponse r = new TransactionResponse();
        r.setId(t.getId());
        r.setAmount(t.getAmount());
        r.setDate(t.getDate());
        r.setCategory(t.getCategory().getName());
        r.setType(t.getCategory().getType().name());
        r.setDescription(t.getDescription());
        r.setNotes(t.getNotes());
        r.setRecurring(t.isRecurring());
        r.setRecurrenceType(t.getRecurrenceType() != null ? t.getRecurrenceType().name() : null);
        return r;
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
