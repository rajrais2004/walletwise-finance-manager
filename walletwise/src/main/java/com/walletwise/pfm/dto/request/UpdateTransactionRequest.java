package com.walletwise.pfm.dto.request;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class UpdateTransactionRequest {

    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    private String category;
    private String description;
    private String notes;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
