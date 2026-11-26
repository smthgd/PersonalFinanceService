package org.example.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;

public final class Console {
    private Console() {
    }

    public static void run(Scanner input, PrintStream output) {
        UUID currentUser = null;
        output.println("""
                Здравствуйте! Доступные функции:
                    income для того, чтобы ввести доходы
                    outcome для того, чтобы ввести расходы
                    income category для создания категории дохода
                    outcome category для создания категории дохода
                    budget для того, чтобы ввести бюджеты на категории
                    exit для выхода
                """);

        while (input.hasNextLine()) {
            String line = input.nextLine().strip();
            if (line.isEmpty()) {
                continue;
            }
            String[] command = line.split("\\s+");
            try {
                switch (command[0].toLowerCase(Locale.ROOT)) {
                    case "exit" -> {
                        if (command.length == 1) {
                            return;
                        }
                        output.println("Использование: exit");
                    }
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                output.println(e.getMessage());
            }
        }
    }
}
