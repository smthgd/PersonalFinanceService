package org.example.cli;

import org.example.core.AuthService;
import org.example.core.User;
import org.example.core.Wallet;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

public final class Console {
    private Console() {
    }

    public static void run(Scanner input, PrintStream output) {
        AuthService auth = new AuthService();
        User currentUser = null;
        output.println("""
                Здравствуйте! Доступные команды:
                    register — создать учётную запись
                    login — войти
                    logout — выйти из учётной записи
                    income-category — создать категорию доходов
                    expense-category — создать категорию расходов
                    income — добавить доход
                    expense — добавить расход
                    budget — установить бюджет категории расходов
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
                    return;
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
                        return;
                    }
                    String login = input.nextLine().strip();
                    output.println("Пароль:");
                    if (!input.hasNextLine()) {
                        return;
                    }
                    char[] password = input.nextLine().toCharArray();
                    try {
                        if (command[0].equalsIgnoreCase("register")) {
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
                    } catch (IllegalArgumentException e) {
                        output.println(e.getMessage());
                    } finally {
                        Arrays.fill(password, '\0');
                    }
                }
                case "income-category", "expense-category", "income", "expense", "budget" -> {
                    if (currentUser == null) {
                        output.println("Сначала войдите в учётную запись.");
                    } else {
                        updateWallet(action, currentUser.wallet(), input, output);
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
        try {
            switch (command) {
                case "income-category" -> wallet.createIncomeCategory(name);
                case "expense-category" -> wallet.createExpenseCategory(name);
                case "income", "expense", "budget" -> {
                    output.println("Сумма (например, 1500.50):");
                    if (!input.hasNextLine()) {
                        return;
                    }
                    BigDecimal amount = new BigDecimal(input.nextLine().strip().replace(',', '.'));
                    switch (command) {
                        case "income" -> wallet.addIncome(name, amount);
                        case "expense" -> wallet.addExpense(name, amount);
                        case "budget" -> wallet.setBudget(name, amount);
                    }
                }
            }
            output.println("Изменения сохранены.");
        } catch (NumberFormatException e) {
            output.println("Введите корректную сумму.");
        } catch (IllegalArgumentException e) {
            output.println(e.getMessage());
        }
    }
}
