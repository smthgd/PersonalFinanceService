package org.example.cli;

import org.example.core.AuthService;
import org.example.core.User;

import java.io.PrintStream;
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

            switch (command[0].toLowerCase(Locale.ROOT)) {
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
                default -> output.println("Неизвестная команда.");
            }
        }
    }
}
