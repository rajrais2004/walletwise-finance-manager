package com.walletwise.pfm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "INCOME|EXPENSE", message = "Type must be INCOME or EXPENSE")
    private String type;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
