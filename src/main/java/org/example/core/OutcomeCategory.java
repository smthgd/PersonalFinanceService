package org.example.core;

import java.math.BigDecimal;

public class OutcomeCategory extends Category {
    private BigDecimal monthlyBudget;
    private BigDecimal spent;

    public OutcomeCategory(String name, double monthlyBudget, double remainingBudget) {
        this(name, BigDecimal.valueOf(monthlyBudget),
                BigDecimal.valueOf(monthlyBudget).subtract(BigDecimal.valueOf(remainingBudget)));
    }

    OutcomeCategory(String name, BigDecimal monthlyBudget, BigDecimal spent) {
        super(name);
        this.monthlyBudget = monthlyBudget;
        this.spent = spent;
    }

    BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }

    BigDecimal spent() {
        return spent;
    }

    BigDecimal getRemainingBudget() {
        return monthlyBudget.subtract(spent);
    }

    void addExpense(BigDecimal amount) {
        spent = spent.add(amount);
    }

    void setBudget(BigDecimal amount) {
        monthlyBudget = amount;
    }
}
