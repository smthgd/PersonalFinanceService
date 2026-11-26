package org.example.core;

import java.util.UUID;

public interface WalletStore {
    boolean addUser(UUID user);

    boolean hasUser(UUID user);

    void removeUser(UUID user);
}
