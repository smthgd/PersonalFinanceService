package org.example;

import org.example.cli.Console;
import org.example.core.AuthService;
import org.example.infrastructure.FileWalletStore;

import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var store = new FileWalletStore(Path.of(".finance-data"));
        Console.run(new Scanner(System.in), System.out, new AuthService(store));
    }
}
