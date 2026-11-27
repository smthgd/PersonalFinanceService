package org.example.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.example.core.WalletFileFormat.requireLine;
import static org.example.core.WalletFileFormat.writeLine;

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

    public BigDecimal budgetOverrun(String name) {
        OutcomeCategory category = findCategory(outcomeCategories, name);
        if (category.getMonthlyBudget() == null) {
            return BigDecimal.ZERO;
        }
        return category.getRemainingBudget().negate().max(BigDecimal.ZERO);
    }

    public BigDecimal totalIncome() {
        return income;
    }

    public BigDecimal totalExpenses() {
        return outcome;
    }

    public BigDecimal incomeByCategories(Collection<String> names) {
        return sumCategories(incomeCategories, names, IncomeCategory::amount);
    }

    public BigDecimal expensesByCategories(Collection<String> names) {
        return sumCategories(outcomeCategories, names, OutcomeCategory::spent);
    }

    private static <T extends Category> BigDecimal sumCategories(
            Map<String, T> categories, Collection<String> names, Function<T, BigDecimal> amount) {
        BigDecimal total = BigDecimal.ZERO;
        Set<String> counted = new HashSet<>();
        for (String name : names) {
            T category = findCategory(categories, name);
            if (counted.add(category.name())) {
                total = total.add(amount.apply(category));
            }
        }
        return total;
    }

    void writeTo(BufferedWriter output) throws IOException {
        writeLine(output, Integer.toString(incomeCategories.size()));
        for (String category : incomeCategories.keySet()) {
            writeLine(output, category);
        }

        writeLine(output, Integer.toString(outcomeCategories.size()));
        for (OutcomeCategory category : outcomeCategories.values()) {
            writeLine(output, category.name());
            BigDecimal budget = category.getMonthlyBudget();
            writeLine(output, budget == null ? "" : budget.toPlainString());
        }

        writeLine(output, Integer.toString(transactions.size()));
        for (Transaction transaction : transactions) {
            writeLine(output, transaction.type().name());
            writeLine(output, transaction.category());
            writeLine(output, transaction.amount().toPlainString());
        }
    }

    void readFrom(BufferedReader input) throws IOException {
        for (int count = WalletFileFormat.readCount(input); count > 0; count--) {
            createIncomeCategory(requireLine(input));
        }
        for (int count = WalletFileFormat.readCount(input); count > 0; count--) {
            String category = requireLine(input);
            createExpenseCategory(category);
            String budget = requireLine(input);
            if (!budget.isEmpty()) {
                setBudget(category, new BigDecimal(budget));
            }
        }
        for (int count = WalletFileFormat.readCount(input); count > 0; count--) {
            Transaction.Type type = Transaction.Type.valueOf(requireLine(input));
            String category = requireLine(input);
            BigDecimal amount = new BigDecimal(requireLine(input));
            switch (type) {
                case INCOME -> addIncome(category, amount);
                case EXPENSE -> addExpense(category, amount);
            }
        }
        if (input.readLine() != null) {
            throw new IOException("Обнаружены лишние данные в файле кошелька.");
        }
    }

    public String report() {
        StringBuilder report = new StringBuilder();
        report.append("Общий доход: ").append(totalIncome().toPlainString()).append('\n');
        report.append("Доходы по категориям:\n");
        for (IncomeCategory category : incomeCategories.values()) {
            report.append('\t').append(category.name()).append(": ")
                    .append(category.amount().toPlainString()).append('\n');
        }

        report.append("Общие расходы: ").append(totalExpenses().toPlainString()).append('\n');
        report.append("Расходы и бюджеты по категориям:\n");
        for (OutcomeCategory category : outcomeCategories.values()) {
            report.append('\t').append(category.name()).append(": ")
                    .append(category.spent().toPlainString());
            BigDecimal budget = category.getMonthlyBudget();
            if (budget == null) {
                report.append(", бюджет не установлен");
            } else {
                report.append(", бюджет: ").append(budget.toPlainString())
                        .append(", оставшийся лимит: ").append(category.getRemainingBudget().toPlainString());
            }
            report.append('\n');
        }
        return report.toString();
    }

    private static String categoryName(String name) {
        String trimmed = name.strip();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Введите название категории.");
        }
        if (trimmed.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Название категории содержит управляющий символ.");
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
