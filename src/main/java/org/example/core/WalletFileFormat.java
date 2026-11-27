package org.example.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;

final class WalletFileFormat {
    private WalletFileFormat() {
    }

    static void writeLine(BufferedWriter output, String value) throws IOException {
        output.write(value);
        output.newLine();
    }

    static String requireLine(BufferedReader input) throws IOException {
        String line = input.readLine();
        if (line == null) {
            throw new EOFException("Файл кошелька обрывается.");
        }
        return line;
    }

    static int readCount(BufferedReader input) throws IOException {
        int count = Integer.parseInt(requireLine(input));
        if (count < 0) {
            throw new IOException("Отрицательное число записей в файле кошелька.");
        }
        return count;
    }
}
