package org.example.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class AuthService {
    private final Map<String, User> users = new HashMap<>();

    private final WalletStore store;

    public AuthService() {
        this.store = null;
    }

    public AuthService(WalletStore store) {
        this.store = store;
    }

    public void register(String login, char[] password) {
        login = validateLogin(login);
        if (CharBuffer.wrap(password).chars().allMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("Пароль не может быть пустым.");
        }
        if (users.containsKey(login) || savedAccountExists(login)) {
            throw new IllegalArgumentException("Этот логин уже занят.");
        }
        users.put(login, new User(login, password));
    }

    private boolean savedAccountExists(String login) {
        try {
            return store != null && store.exists(login);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось проверить наличие учётной записи: " + e.getMessage(), e);
        }
    }

    public Optional<User> login(String login, char[] password) {
        login = validateLogin(login);
        User user = users.get(login);
        if (user != null) {
            return user.matchesPassword(password) ? Optional.of(user) : Optional.empty();
        }
        if (store == null) {
            return Optional.empty();
        }

        try (var input = store.open(login)) {
            user = User.readCredentials(login, input);
            if (!user.matchesPassword(password)) {
                return Optional.empty();
            }
            user.wallet().readFrom(input);
        } catch (NoSuchFileException e) {
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось загрузить кошелёк: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Файл кошелька повреждён.", e);
        }

        users.put(login, user);
        return Optional.of(user);
    }

    public void saveWallets() throws IOException {
        if (store == null) {
            return;
        }
        IOException failure = null;
        for (User user : users.values()) {
            try {
                store.save(user.login(), user::write);
            } catch (IOException e) {
                if (failure == null) {
                    failure = new IOException("Не удалось сохранить кошельки.");
                }
                String details = "Кошелёк «" + user.login() + "»: " + e.getMessage();
                failure.addSuppressed(new IOException(details, e));
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    private static String validateLogin(String login) {
        String trimmed = login.strip();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Логин не может быть пустым.");
        }
        if (trimmed.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Логин содержит управляющий символ.");
        }
        return trimmed;
    }
}
