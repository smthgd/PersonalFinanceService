package org.example.core;

import java.util.List;

public class Wallet {
    private final double income;
    private final double outcome;
    private final List<IncomeCategory> incomeCategories;
    private final List<OutcomeCategory> outcomeCategories;

    public Wallet(double income, double outcome) {
        this(income, outcome, List.of(), List.of());
    }

    public Wallet(double income, double outcome, List<IncomeCategory> incomeCategories,
                  List<OutcomeCategory> outcomeCategories) {
        this.income = income;
        this.outcome = outcome;
        this.incomeCategories = List.copyOf(incomeCategories);
        this.outcomeCategories = List.copyOf(outcomeCategories);
    }

    public void printWallet() {
        System.out.println("Общий доход: " + income);
        System.out.println("Доходы по категориям:");
        for (IncomeCategory incomeCategory : incomeCategories) {
            System.out.println("\t" + incomeCategory.name() + ": " + incomeCategory.amount());
        }
        System.out.println("Общие расходы: " + this.outcome);
        System.out.println("Бюджет по категориям:");
        for (OutcomeCategory outcomeCategory : outcomeCategories) {
            System.out.println("\t" + outcomeCategory.name() + ": " + outcomeCategory.getMonthlyBudget() + ", Оставшийся бюджет: " + outcomeCategory.getRemainingBudget());
        }
    }
}
