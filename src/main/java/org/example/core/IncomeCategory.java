package org.example.core;

import java.math.BigDecimal;

public class IncomeCategory extends Category {
    private BigDecimal amount;

    public IncomeCategory(String name, double amount) {
        this(name, BigDecimal.valueOf(amount));
    }

    IncomeCategory(String name, BigDecimal amount) {
        super(name);
        this.amount = amount;
    }

    BigDecimal amount() {
        return amount;
    }

    void addIncome(BigDecimal amount) {
        this.amount = this.amount.add(amount);
    }
}
