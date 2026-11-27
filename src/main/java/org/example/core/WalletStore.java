package org.example.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public interface WalletStore {
    boolean exists(String login) throws IOException;

    BufferedReader open(String login) throws IOException;

    void save(String login, ContentWriter writer) throws IOException;

    @FunctionalInterface
    interface ContentWriter {
        void write(BufferedWriter output) throws IOException;
    }
}
