package org.example.infrastructure;

import org.example.core.WalletStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class FileWalletStore implements WalletStore {
    private final Path directory;

    public FileWalletStore(Path directory) {
        this.directory = directory;
    }

    @Override
    public boolean exists(String login) throws IOException {
        try {
            Files.readAttributes(pathFor(login), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            return true;
        } catch (NoSuchFileException e) {
            return false;
        }
    }

    @Override
    public BufferedReader open(String login) throws IOException {
        return Files.newBufferedReader(pathFor(login));
    }

    @Override
    public void save(String login, ContentWriter writer) throws IOException {
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "wallet-", ".tmp");
        try {
            try (var output = Files.newBufferedWriter(temporary)) {
                writer.write(output);
            }
            Files.move(temporary, pathFor(login), StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private Path pathFor(String login) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(login.getBytes(StandardCharsets.UTF_8));
            return directory.resolve(HexFormat.of().formatHex(digest) + ".wallet");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
