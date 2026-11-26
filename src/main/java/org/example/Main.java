package org.example;

import org.example.cli.Console;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Console.run(new Scanner(System.in), System.out);
    }
}
