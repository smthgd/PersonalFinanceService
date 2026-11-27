package org.example.core;

import java.math.BigDecimal;

public record Transaction(Type type, String category, BigDecimal amount) {
    public enum Type {
        INCOME,
        EXPENSE
    }
}
