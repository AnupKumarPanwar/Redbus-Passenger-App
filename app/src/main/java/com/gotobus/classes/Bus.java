package com.gotobus.classes;

public class Bus {
    public final String routeId;
    public final String name;
    public final String fare;
    public final String type;
    public final String departureTime;
    public final String arrivalTime;

    public Bus(String routeId, String name, String fare, String type, String departureTime, String arrivalTime) {
        this.routeId = routeId;
        this.name = name;
        this.fare = fare;
        this.type = type;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }
}
