package org.example.core;

public class OutcomeCategory extends Category {
    private final double monthlyBudget;
    private final double remainingBudget;

    public OutcomeCategory(String name, double monthlyBudget, double remainingBudget){
        super(name);
        this.monthlyBudget = monthlyBudget;
        this.remainingBudget = remainingBudget;
    }
}
