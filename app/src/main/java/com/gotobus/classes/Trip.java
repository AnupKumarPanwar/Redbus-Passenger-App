package com.gotobus.classes;

public class Trip {
    public final String date;
    public final String bus;
    public final String fare;
    public final String type;
    public final String status;
    public final String source;
    public final String destination;

    public Trip(String date, String bus, String fare, String type, String status, String source, String destination) {
        this.date = date;
        this.bus = bus;
        this.fare = fare;
        this.type = type;
        this.status = status;
        this.source = source;
        this.destination = destination;
    }
}
