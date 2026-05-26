package com.walletwise.pfm.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "transaction_categories")
public class TransactionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CategoryType type;

    @Column(name = "is_custom", nullable = false)
    private boolean isCustom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public enum CategoryType {
        INCOME, EXPENSE
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }
    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
