package org.example.infrastructure;

import org.example.core.Transaction;
import org.example.core.Wallet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

public final class WalletCsv {
    private static final String HEADER = "type,category,amount";
    private static final Pattern AMOUNT = Pattern.compile("-?[0-9]+(?:[.][0-9]+)?");

    private WalletCsv() {
    }

    public static void exportTo(Path path, Wallet wallet) throws IOException {
        try (var output = Files.newBufferedWriter(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            output.write(HEADER);
            output.newLine();
            for (String name : wallet.incomeCategoryNames()) {
                writeRow(output, "INCOME_CATEGORY", name, "");
            }
            for (String name : wallet.expenseCategoryNames()) {
                String budget = wallet.budgetFor(name).map(BigDecimal::toPlainString).orElse("");
                writeRow(output, "EXPENSE_CATEGORY", name, budget);
            }
            for (Transaction transaction : wallet.transactions()) {
                writeRow(output, transaction.type().name(), transaction.category(),
                        transaction.amount().toPlainString());
            }
        }
    }

    public static Wallet importFrom(Path path) throws IOException {
        Wallet wallet = new Wallet(0, 0);
        try (var input = Files.newBufferedReader(path)) {
            String header = input.readLine();
            if (!HEADER.equals(header) && !("\uFEFF" + HEADER).equals(header)) {
                throw new IOException("Ожидался заголовок CSV: " + HEADER);
            }

            String line;
            int lineNumber = 1;
            while ((line = input.readLine()) != null) {
                lineNumber++;
                try {
                    readRow(wallet, line);
                } catch (IllegalArgumentException e) {
                    throw new IOException("Строка " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        }
        return wallet;
    }

    private static void writeRow(BufferedWriter output, String type, String category, String amount)
            throws IOException {
        output.write(type + ",\"" + category.replace("\"", "\"\"") + "\"," + amount);
        output.newLine();
    }

    private static void readRow(Wallet wallet, String line) {
        int firstComma = line.indexOf(',');
        int lastComma = line.lastIndexOf(',');
        if (firstComma < 0 || firstComma == lastComma) {
            throw new IllegalArgumentException("Ожидались три поля CSV.");
        }

        String type = line.substring(0, firstComma);
        String category = readCategory(line.substring(firstComma + 1, lastComma));
        String amount = line.substring(lastComma + 1);
        switch (type) {
            case "INCOME_CATEGORY" -> {
                if (!amount.isEmpty()) {
                    throw new IllegalArgumentException("Для категории доходов поле суммы должно быть пустым.");
                }
                wallet.createIncomeCategory(category);
            }
            case "EXPENSE_CATEGORY" -> {
                wallet.createExpenseCategory(category);
                if (!amount.isEmpty()) {
                    wallet.setBudget(category, readAmount(amount));
                }
            }
            case "INCOME" -> wallet.addIncome(category, readAmount(amount));
            case "EXPENSE" -> wallet.addExpense(category, readAmount(amount));
            default -> throw new IllegalArgumentException("Неизвестный тип записи: " + type);
        }
    }

    private static String readCategory(String field) {
        if (field.length() >= 2 && field.startsWith("\"") && field.endsWith("\"")) {
            String quoted = field.substring(1, field.length() - 1);
            if (!quoted.replace("\"\"", "").contains("\"")) {
                return quoted.replace("\"\"", "\"");
            }
        } else if (!field.contains(",") && !field.contains("\"")) {
            return field;
        }
        throw new IllegalArgumentException("Некорректные кавычки в названии категории.");
    }

    private static BigDecimal readAmount(String value) {
        if (!AMOUNT.matcher(value).matches()) {
            throw new IllegalArgumentException("Ожидалась десятичная сумма с точкой.");
        }
        return new BigDecimal(value);
    }
}
