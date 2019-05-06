package com.gotobus.classes;

public class Cashback {
    public final String id;
    public final String amount;
    public String status;

    public Cashback(String id, String amount, String status) {
        this.id = id;
        this.amount = amount;
        this.status = status;
    }
}
