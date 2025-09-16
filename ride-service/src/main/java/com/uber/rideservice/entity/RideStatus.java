package com.uber.rideservice.entity;
public enum RideStatus {
    REQUESTED,         // User initialized the ride request
    DRIVER_RESERVED,   // A driver has been assigned/notified, awaiting acceptance
    CONFIRMED,         // Driver accepted the ride
    IN_PROGRESS,       // Ride has started
    COMPLETED,         // Ride has finished
    CANCELLED          // Ride was cancelled by user or driver, or timed out
}