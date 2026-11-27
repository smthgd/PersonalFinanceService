package org.example.core;

import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class AuthService {
    private final Map<String, User> users = new HashMap<>();

    public void register(String login, char[] password) {
        login = validateLogin(login);
        if (CharBuffer.wrap(password).chars().allMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("Пароль не может быть пустым.");
        }
        if (users.containsKey(login)) {
            throw new IllegalArgumentException("Этот логин уже занят.");
        }
        users.put(login, new User(login, password));
    }

    public Optional<User> login(String login, char[] password) {
        User user = users.get(validateLogin(login));
        if (user != null && user.matchesPassword(password)) {
            return Optional.of(user);
        }
        return Optional.empty();
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
