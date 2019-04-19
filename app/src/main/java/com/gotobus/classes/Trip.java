package com.gotobus.classes;

public class Trip {
    public String date, bus, fare, type, status, source, destination;

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
