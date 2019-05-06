package com.gotobus.utility;

import java.util.ArrayList;

public class Journey {
    public static String sourceLat, sourceLng, destinationLat, destinationLng, otp, pickupAddress, dropoffAddress, journeyDate, busName;
    public static int fare;
    public static ArrayList<String> seats;

    public static String getSeats() {
        return seats.toString();
    }
}
