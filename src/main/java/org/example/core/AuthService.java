package org.example.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class AuthService {
    private final Map<String, User> users = new HashMap<>();

    public void register(String login, char[] password) {
        if (login.isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым.");
        }
        if (password.length == 0) {
            throw new IllegalArgumentException("Пароль не может быть пустым.");
        }
        if (users.containsKey(login)) {
            throw new IllegalArgumentException("Этот логин уже занят.");
        }
        users.put(login, new User(login, password));
    }

    public Optional<User> login(String login, char[] password) {
        User user = users.get(login);
        if (user != null && user.matchesPassword(password)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }
}
