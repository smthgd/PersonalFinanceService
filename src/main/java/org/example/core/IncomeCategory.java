package org.example.core;

public class IncomeCategory extends Category {
    private final double amount;

    public IncomeCategory(String name, double amount){
        super(name);
        this.amount = amount;
    }

    double amount(){
        return amount;
    }
}
