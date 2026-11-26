package org.example.core;

import java.util.List;

public class Wallet {
    private final double Income;
    private final double Outcome;
    private List<IncomeCategory> incomeCategories;
    private List<OutcomeCategory> outcomeCategories;

    public Wallet(double income, double outcome) {
        this.Income = income;
        this.Outcome = outcome;
    }

    public void printWallet() {
        System.out.println("Общий доход: " + Income);
        System.out.println("Доходы по категориям:");
        for (IncomeCategory incomeCategory : incomeCategories) {
            System.out.println("\t" + incomeCategory.name() + ": " + incomeCategory.amount() + "\n");
        }
        System.out.println("Общие расходы: " + this.Outcome);
        System.out.println("Бюджет по категориям:");
        for (OutcomeCategory outcomeCategory : outcomeCategories) {
            System.out.println("\t" + outcomeCategory.name() + ": " + outcomeCategory.getMonthlyBudget() + ", Оставшийся бюджет: " + outcomeCategory.getRemainingBudget() + "\n");
        }
    }
}
