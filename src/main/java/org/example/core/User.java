package org.example.core;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import static org.example.core.WalletFileFormat.requireLine;
import static org.example.core.WalletFileFormat.writeLine;

public final class User {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int HASH_ITERATIONS = 600_000;
    private static final int HASH_BITS = 256;

    private final UUID uuid;
    private Wallet wallet = new Wallet(0, 0);
    private final String login;
    private final byte[] salt;
    private final byte[] passwordHash;

    User(String login, char[] password) {
        this.uuid = UUID.randomUUID();
        this.login = login;
        this.salt = new byte[16];
        RANDOM.nextBytes(salt);
        this.passwordHash = hash(password, salt);
    }

    private User(UUID uuid, String login, byte[] salt, byte[] passwordHash) {
        this.uuid = uuid;
        this.login = login;
        this.salt = salt;
        this.passwordHash = passwordHash;
    }

    static User readCredentials(String login, BufferedReader input) throws IOException {
        if (!"personal-finance-v1".equals(input.readLine()) || !login.equals(input.readLine())) {
            throw new IOException("Неизвестный формат файла или неверный логин.");
        }
        UUID uuid = UUID.fromString(requireLine(input));
        byte[] salt = Base64.getDecoder().decode(requireLine(input));
        byte[] hash = Base64.getDecoder().decode(requireLine(input));
        if (salt.length != 16 || hash.length != HASH_BITS / Byte.SIZE) {
            throw new IOException("Повреждены учётные данные.");
        }
        return new User(uuid, login, salt, hash);
    }

    void write(BufferedWriter output) throws IOException {
        writeLine(output, "personal-finance-v1");
        writeLine(output, login);
        writeLine(output, uuid.toString());
        writeLine(output, Base64.getEncoder().encodeToString(salt));
        writeLine(output, Base64.getEncoder().encodeToString(passwordHash));
        wallet.writeTo(output);
    }

    public UUID uuid() {
        return uuid;
    }

    public String login() {
        return login;
    }

    public Wallet wallet() {
        return wallet;
    }

    public void importWallet(Wallet imported) {
        if (!wallet.isEmpty()) {
            throw new IllegalStateException("Импорт доступен только в пустой кошелёк.");
        }
        wallet = Objects.requireNonNull(imported);
    }

    boolean matchesPassword(char[] password) {
        return MessageDigest.isEqual(passwordHash, hash(password, salt));
    }

    private static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, HASH_ITERATIONS, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Не удалось обработать пароль.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
