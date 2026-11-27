package org.example.cli;

import org.example.core.AuthService;
import org.example.core.User;
import org.example.core.Wallet;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

public final class Console {
    private static final Pattern DECIMAL_AMOUNT =
            Pattern.compile("[+-]?(?:[0-9]+(?:[.,][0-9]*)?|[.,][0-9]+)");

    private Console() {
    }

    public static void run(Scanner input, PrintStream output, AuthService auth) {
        User currentUser = null;
        output.println("""
                Здравствуйте! Доступные команды:
                    register — создать учётную запись
                    login — войти
                    logout — выйти из учётной записи
                    income-category — создать категорию доходов
                    expense-category — создать категорию расходов
                    rename-income-category — переименовать категорию доходов
                    rename-expense-category — переименовать категорию расходов
                    delete-income-category — удалить категорию доходов без операций
                    delete-expense-category — удалить категорию расходов без операций
                    income — добавить доход
                    expense — добавить расход
                    budget — установить бюджет категории расходов
                    sum-income — подсчитать доходы
                    sum-expenses — подсчитать расходы
                    report — вывести отчёт по кошельку
                    report-file — записать отчёт в новый файл
                    exit — завершить приложение
                """);

        while (input.hasNextLine()) {
            String line = input.nextLine().strip();
            if (line.isEmpty()) {
                continue;
            }
            String[] command = line.split("\\s+", 2);
            if (command.length != 1) {
                output.println("Введите команду без аргументов.");
                continue;
            }

            String action = command[0].toLowerCase(Locale.ROOT);
            switch (action) {
                case "exit" -> {
                    if (saveWallets(auth, output)) {
                        return;
                    }
                    output.println("После устранения ошибки повторите exit.");
                }
                case "logout" -> {
                    if (currentUser == null) {
                        output.println("Сначала войдите в учётную запись.");
                    } else {
                        currentUser = null;
                        output.println("Вы вышли из учётной записи.");
                    }
                }
                case "register", "login" -> {
                    if (currentUser != null) {
                        output.println("Сначала выйдите из учётной записи командой logout.");
                        continue;
                    }

                    output.println("Логин:");
                    if (!input.hasNextLine()) {
                        break;
                    }
                    String login = input.nextLine();
                    output.println("Пароль:");
                    if (!input.hasNextLine()) {
                        break;
                    }
                    char[] password = input.nextLine().toCharArray();
                    try {
                        if (action.equals("register")) {
                            auth.register(login, password);
                            output.println("Учётная запись создана. Для входа введите login.");
                        } else {
                            currentUser = auth.login(login, password).orElse(null);
                            if (currentUser == null) {
                                output.println("Неверный логин или пароль.");
                            } else {
                                output.println("Вы вошли как " + currentUser.login() + ".");
                            }
                        }
                    } catch (IllegalArgumentException | IllegalStateException | UncheckedIOException e) {
                        output.println(e.getMessage());
                    } finally {
                        Arrays.fill(password, '\0');
                    }
                }
                case "income-category", "expense-category", "rename-income-category", "rename-expense-category",
                     "delete-income-category", "delete-expense-category", "income", "expense", "budget" -> {
                    if (currentUser == null) {
                        output.println("Сначала войдите в учётную запись.");
                    } else {
                        updateWallet(action, currentUser.wallet(), input, output);
                    }
                }
                case "sum-income", "sum-expenses" -> {
                    if (currentUser == null) {
                        output.println("Сначала войдите в учётную запись.");
                    } else {
                        printSum(currentUser.wallet(), action.equals("sum-income"), input, output);
                    }
                }
                case "report", "report-file" -> {
                    if (currentUser == null) {
                        output.println("Сначала войдите в учётную запись.");
                    } else if (action.equals("report")) {
                        output.print(currentUser.wallet().report());
                    } else {
                        writeReport(currentUser.wallet(), input, output);
                    }
                }
                default -> output.println("Неизвестная команда.");
            }
        }
        saveWallets(auth, output);
    }

    private static boolean saveWallets(AuthService auth, PrintStream output) {
        try {
            auth.saveWallets();
            return true;
        } catch (IOException e) {
            output.println(e.getMessage());
            for (Throwable detail : e.getSuppressed()) {
                output.println(detail.getMessage());
            }
            return false;
        }
    }

    private static void printSum(Wallet wallet, boolean income, Scanner input, PrintStream output) {
        output.println("Введите названия категорий, каждое с новой строки. Пустая строка завершает ввод.");
        output.println("Чтобы подсчитать общую сумму, сразу введите пустую строку.");
        List<String> categories = new ArrayList<>();
        while (input.hasNextLine()) {
            String name = input.nextLine();
            if (name.isBlank()) {
                try {
                    BigDecimal total;
                    if (categories.isEmpty()) {
                        total = income ? wallet.totalIncome() : wallet.totalExpenses();
                    } else {
                        total = income
                                ? wallet.incomeByCategories(categories)
                                : wallet.expensesByCategories(categories);
                    }
                    output.println((income ? "Сумма доходов: " : "Сумма расходов: ") + total.toPlainString());
                } catch (IllegalArgumentException e) {
                    output.println(e.getMessage());
                }
                return;
            }
            categories.add(name);
        }
    }

    private static void writeReport(Wallet wallet, Scanner input, PrintStream output) {
        output.println("Путь к новому файлу:");
        if (!input.hasNextLine()) {
            return;
        }
        String filename = input.nextLine();
        if (filename.isBlank()) {
            output.println("Введите путь к файлу.");
            return;
        }

        try {
            Path path = Path.of(filename);
            Files.writeString(path, wallet.report(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            output.println("Отчёт записан в " + path + ".");
        } catch (InvalidPathException e) {
            output.println("Некорректный путь к файлу.");
        } catch (FileAlreadyExistsException e) {
            output.println("Файл уже существует. Укажите другой путь.");
        } catch (IOException e) {
            output.println("Не удалось записать отчёт: " + e.getMessage());
        }
    }

    private static void updateWallet(String command, Wallet wallet, Scanner input, PrintStream output) {
        output.println("Название категории:");
        if (!input.hasNextLine()) {
            return;
        }
        String name = input.nextLine();
        if (name.isBlank()) {
            output.println("Введите название категории.");
            return;
        }
        try {
            switch (command) {
                case "income-category" -> wallet.createIncomeCategory(name);
                case "expense-category" -> wallet.createExpenseCategory(name);
                case "delete-income-category" -> wallet.deleteIncomeCategory(name);
                case "delete-expense-category" -> wallet.deleteExpenseCategory(name);
                case "rename-income-category", "rename-expense-category" -> {
                    output.println("Новое название категории:");
                    if (!input.hasNextLine()) {
                        return;
                    }
                    String newName = input.nextLine();
                    if (command.equals("rename-income-category")) {
                        wallet.renameIncomeCategory(name, newName);
                    } else {
                        wallet.renameExpenseCategory(name, newName);
                    }
                }
                case "income", "expense", "budget" -> {
                    output.println("Сумма (например, 1500.50):");
                    if (!input.hasNextLine()) {
                        return;
                    }
                    String value = input.nextLine().strip();
                    if (!DECIMAL_AMOUNT.matcher(value).matches()) {
                        output.println("Введите сумму десятичным числом, например 1500.50 или 1500,50.");
                        return;
                    }
                    BigDecimal amount = new BigDecimal(value.replace(',', '.'));
                    switch (command) {
                        case "income" -> wallet.addIncome(name, amount);
                        case "expense" -> wallet.addExpense(name, amount);
                        case "budget" -> wallet.setBudget(name, amount);
                    }
                }
            }
            output.println("Изменения сохранены.");
            if (command.equals("expense") || command.equals("budget")) {
                BigDecimal overrun = wallet.budgetOverrun(name);
                if (overrun.signum() > 0) {
                    output.println("Внимание: бюджет категории «" + name.strip()
                            + "» превышен на " + overrun.toPlainString() + ".");
                }
            }
            if (command.equals("expense")) {
                BigDecimal deficit = wallet.totalExpenses().subtract(wallet.totalIncome());
                if (deficit.signum() > 0) {
                    output.println("Внимание: общие расходы превышают доходы на " + deficit.toPlainString() + ".");
                }
            }
        } catch (IllegalArgumentException e) {
            output.println(e.getMessage());
        }
    }
}
