package org.example.core;

import java.util.UUID;

public class User {
    private final UUID uuid;
    private final String login;
    private final String password;

    public User(UUID uuid, String login, String password) {
        this.uuid = uuid;
        this.login = login;
        this.password = password;
    }
}
