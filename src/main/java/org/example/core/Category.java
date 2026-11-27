package org.example.core;

public abstract class Category {
    private String name;

    public Category(String name) {
        this.name = name;
    }

    String name() {
        return name;
    }

    void rename(String name) {
        this.name = name;
    }
}
