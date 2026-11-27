package org.example.core;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.UUID;

public final class User {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int HASH_ITERATIONS = 600_000;
    private static final int HASH_BITS = 256;

    private final UUID uuid;
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

    public UUID uuid() {
        return uuid;
    }

    public String login() {
        return login;
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
