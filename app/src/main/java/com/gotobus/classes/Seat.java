package com.gotobus.classes;

public class Seat {
    public final String id;
    public String status;

    public Seat(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
