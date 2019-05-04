package com.gotobus.classes;

public class Bus {
    public String routeId, name, fare, type, departureTime, arrivalTime;

    public Bus(String routeId, String name, String fare, String type, String departureTime, String arrivalTime) {
        this.routeId = routeId;
        this.name = name;
        this.fare = fare;
        this.type = type;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }
}
