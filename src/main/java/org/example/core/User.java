package org.example.core;

import java.util.UUID;

public class User {
    private UUID uuid;
    private String login;
    private String password;

    public User(UUID uuid, String login, String password){
        this.uuid = uuid;
        this.login = login;
        this.password = password;
    }
}
