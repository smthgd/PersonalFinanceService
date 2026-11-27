package org.example.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Wallet {
    private BigDecimal income;
    private BigDecimal outcome;
    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<String, IncomeCategory> incomeCategories = new LinkedHashMap<>();
    private final Map<String, OutcomeCategory> outcomeCategories = new LinkedHashMap<>();

    public Wallet(double income, double outcome) {
        this(income, outcome, List.of(), List.of());
    }

    public Wallet(double income, double outcome, List<IncomeCategory> incomeCategories,
                  List<OutcomeCategory> outcomeCategories) {
        this.income = BigDecimal.valueOf(income);
        this.outcome = BigDecimal.valueOf(outcome);
        for (IncomeCategory category : incomeCategories) {
            this.incomeCategories.put(category.name(), new IncomeCategory(category.name(), category.amount()));
        }
        for (OutcomeCategory category : outcomeCategories) {
            this.outcomeCategories.put(category.name(),
                    new OutcomeCategory(category.name(), category.getMonthlyBudget(), category.spent()));
        }
    }

    public void createIncomeCategory(String name) {
        name = categoryName(name);
        addCategory(incomeCategories, new IncomeCategory(name, BigDecimal.ZERO));
    }

    public void createExpenseCategory(String name) {
        name = categoryName(name);
        addCategory(outcomeCategories, new OutcomeCategory(name, null, BigDecimal.ZERO));
    }

    public void addIncome(String name, BigDecimal amount) {
        requirePositiveAmount(amount);
        IncomeCategory category = findCategory(incomeCategories, name);
        BigDecimal total = income.add(amount);
        category.addIncome(amount);
        income = total;
        transactions.add(new Transaction(Transaction.Type.INCOME, category.name(), amount));
    }

    public void addExpense(String name, BigDecimal amount) {
        requirePositiveAmount(amount);
        OutcomeCategory category = findCategory(outcomeCategories, name);
        BigDecimal total = outcome.add(amount);
        category.addExpense(amount);
        outcome = total;
        transactions.add(new Transaction(Transaction.Type.EXPENSE, category.name(), amount));
    }

    public List<Transaction> transactions() {
        return List.copyOf(transactions);
    }

    public void setBudget(String name, BigDecimal amount) {
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Бюджет не может быть отрицательным.");
        }
        findCategory(outcomeCategories, name).setBudget(amount);
    }

    public void printWallet() {
        System.out.println("Общий доход: " + income.toPlainString());
        System.out.println("Доходы по категориям:");
        for (IncomeCategory category : incomeCategories.values()) {
            System.out.println("\t" + category.name() + ": " + category.amount().toPlainString());
        }
        System.out.println("Общие расходы: " + outcome.toPlainString());
        System.out.println("Бюджет по категориям:");
        for (OutcomeCategory category : outcomeCategories.values()) {
            if (category.getMonthlyBudget() == null) {
                System.out.println("\t" + category.name() + ": бюджет не установлен");
            } else {
                System.out.println("\t" + category.name() + ": " + category.getMonthlyBudget().toPlainString()
                        + ", Оставшийся бюджет: " + category.getRemainingBudget().toPlainString());
            }
        }
    }

    private static String categoryName(String name) {
        String trimmed = name.strip();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Введите название категории.");
        }
        return trimmed;
    }

    private static <T extends Category> void addCategory(Map<String, T> categories, T category) {
        if (categories.putIfAbsent(category.name(), category) != null) {
            throw new IllegalArgumentException("Категория уже существует: " + category.name());
        }
    }

    private static <T extends Category> T findCategory(Map<String, T> categories, String name) {
        String trimmed = categoryName(name);
        T category = categories.get(trimmed);
        if (category == null) {
            throw new IllegalArgumentException("Категория не найдена: " + trimmed);
        }
        return category;
    }

    private static void requirePositiveAmount(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Сумма операции должна быть больше нуля.");
        }
    }
}
