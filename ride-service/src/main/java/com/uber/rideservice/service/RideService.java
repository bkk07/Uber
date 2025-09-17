package com.uber.rideservice.service;

import com.uber.rideservice.dto.*;
import com.uber.rideservice.dto.NotificationRequest;
import com.uber.rideservice.entity.Ride;
import com.uber.rideservice.entity.RideStatus;
import com.uber.rideservice.exception.RideNotFoundException;
import com.uber.rideservice.feignclient.DriverServiceFeignClient;
import com.uber.rideservice.feignclient.NotificationServiceFeignClient;
import com.uber.rideservice.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {
    private final RideRepository rideRepository;
    private final DriverServiceFeignClient driverServiceFeignClient;
    private final NotificationServiceFeignClient notificationServiceFeignClient;
    private final RedisTemplate<String, Object> redisTemplate;

    // Constants
    private static final long DRIVER_ACCEPTANCE_TIMEOUT_SECONDS = 30;
    private static final String DRIVER_RESERVATION_KEY_PREFIX = "ride:driver:reservation:";

    @Transactional
    public RideResponse initializeRide(InitializeRideRequest request) {
        // 1. Save ride with status = REQUESTED
        Ride ride = new Ride();
        ride.setUserId(request.getUserId());
        ride.setPickupLocation(request.getPickupLocation());
        ride.setDropLocation(request.getDropLocation());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setCreatedAt(LocalDateTime.now());
        ride.setUpdatedAt(LocalDateTime.now());
        // Fare estimation logic would go here
        ride.setFareEstimate(15.00); // Placeholder
        ride = rideRepository.save(ride);

        // 2. Call Driver-Service to fetch top 5 nearby drivers (using pickup location for now)
        // In a real scenario, you'd get user's current location, not just pickup.
        // For simplicity, let's assume `DriverServiceFeignClient` can determine nearby based on pickupLocation string,
        // or we need actual lat/long for pickup. For now, using dummy coordinates.
        List<DriverDto> nearbyDrivers = driverServiceFeignClient.getNearbyDrivers(
                getLatitudeFromLocation(request.getPickupLocation()),
                getLongitudeFromLocation(request.getPickupLocation()),
                5
        );

        log.info("Fetched {} nearby drivers for ride {}", nearbyDrivers.size(), ride.getId());
        // 3. Return rideId + list of drivers to user
        RideResponse response = mapRideToRideResponse(ride);
        response.setNearbyDrivers(nearbyDrivers);
        response.setMessage("Ride request initialized. Please select a driver.");
        return response;
    }

    @Transactional
    public RideResponse selectDriver(Long rideId, SelectDriverRequest request) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with ID: " + rideId));

        if (ride.getStatus() != RideStatus.REQUESTED && ride.getStatus() != RideStatus.CANCELLED) {
            // Allow re-selection if previous driver rejected/timed out (status could be CANCELLED or REQUESTED again)
            // Or if another driver was being processed
            if (ride.getDriverId() != null && ride.getStatus() == RideStatus.DRIVER_RESERVED) {
                // If a driver was already reserved and is awaiting response, cancel that reservation first
                log.warn("Ride {} already has a driver {} reserved. Cancelling previous reservation.", rideId, ride.getDriverId());
                // In a real system, you might notify the previously reserved driver of cancellation.
                redisTemplate.delete(DRIVER_RESERVATION_KEY_PREFIX + rideId + ":" + ride.getDriverId());
            }
        }

        // 1. Update ride → assign driverId, status = DRIVER_RESERVED
        ride.setDriverId(request.getDriverId());
        ride.setStatus(RideStatus.DRIVER_RESERVED);
        ride = rideRepository.save(ride);
        log.info("Driver {} selected for ride {}", request.getDriverId(), rideId);

        // 2. Send notification to that driver (via Notification-Service)
        try {
            notificationServiceFeignClient.createNotification(
                    NotificationRequest
                            .builder()
                            .recipientId(String.valueOf(request.getDriverId()))
                            .recipientType("DRIVER")
                            .notificationType("RIDE_REQUEST")
                            .messageContent("New ride request from user " + ride.getUserId() + " to " + ride.getDropLocation())
                            .build()
            );
            log.info("Notification sent to driver {} for ride {}", request.getDriverId(), rideId);
        } catch (Exception e) {
            log.error("Failed to send notification to driver {}: {}", request.getDriverId(), e.getMessage());
            // Depending on system design, this might trigger a retry or mark notification as failed.
        }

        // 3. Start 30s timer (using Redis TTL)
        String reservationKey = DRIVER_RESERVATION_KEY_PREFIX + rideId + ":" + request.getDriverId();
        redisTemplate.opsForValue().set(reservationKey, "pending", DRIVER_ACCEPTANCE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        log.info("Started 30s timer for driver {} to accept ride {}", request.getDriverId(), rideId);


        RideResponse response = mapRideToRideResponse(ride);
        response.setMessage("Driver " + request.getDriverId() + " has been notified and has " + DRIVER_ACCEPTANCE_TIMEOUT_SECONDS + " seconds to respond.");
        return response;
    }

    @Transactional
    public RideResponse handleDriverResponse(Long rideId, DriverResponseRequest request) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with ID: " + rideId));

        // Check if the response is from the currently assigned driver
        if (!request.getDriverId().equals(ride.getDriverId())) {
            log.warn("Driver {} responded for ride {} but is not the currently assigned driver {}. Ignoring.",
                    request.getDriverId(), rideId, ride.getDriverId());
            // Could return an error or simply ignore
            return mapRideToRideResponse(ride, "Response ignored: Not the assigned driver.");
        }

        // Check if the reservation timed out or was already processed
        String reservationKey = DRIVER_RESERVATION_KEY_PREFIX + rideId + ":" + request.getDriverId();
        if (!redisTemplate.hasKey(reservationKey)) {
            log.warn("Driver {} responded for ride {} but reservation key {} not found (timed out or already processed).",
                    request.getDriverId(), rideId, reservationKey);
            // This means the 30s timer expired or the ride moved to another state
            if (ride.getStatus() == RideStatus.DRIVER_RESERVED) {
                // If the ride is still DRIVER_RESERVED, but Redis key is gone, it timed out.
                // This situation implies a race condition or a delayed response after timeout
                // For simplicity, we will treat it as a rejection if current status is DRIVER_RESERVED.
                return handleDriverTimeout(rideId);
            }
            return mapRideToRideResponse(ride, "Driver response ignored as reservation expired or ride status changed.");
        }

        // Clear the Redis timer as driver has responded
        redisTemplate.delete(reservationKey);

        if (request.getAccepted()) {
            // 1. If accepted within 30s → status = CONFIRMED
            ride.setStatus(RideStatus.CONFIRMED);
            ride = rideRepository.save(ride);
            // Optionally update driver status in Driver-Service (e.g., to "unavailable" or "en route")
            driverServiceFeignClient.updateDriverStatus(request.getDriverId(), "BUSY");
            log.info("Driver {} accepted ride {}", request.getDriverId(), rideId);
            notificationServiceFeignClient.createNotification(
                    NotificationRequest
                            .builder()
                            .recipientId(String.valueOf(ride.getUserId()))
                            .recipientType("USER")
                            .notificationType("RIDE_ACCEPTED")
                            .messageContent("Your ride with driver " + request.getDriverId() + " has been confirmed!")
                            .build()
            );
            return mapRideToRideResponse(ride, "Ride confirmed by driver.");
        } else {
            // 2. If rejected → unassign driver, return next driver option to user.
            log.info("Driver {} rejected ride {}", request.getDriverId(), rideId);
            return handleDriverRejection(rideId);
        }
    }

    @Transactional
    public RideResponse startRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with ID: " + rideId));

        if (ride.getStatus() != RideStatus.CONFIRMED) {
            throw new IllegalStateException("Ride cannot be started as its status is not CONFIRMED. Current status: " + ride.getStatus());
        }
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride = rideRepository.save(ride);
        // Update driver status in Driver-Service (e.g., to "in_ride")
        driverServiceFeignClient.updateDriverStatus(ride.getDriverId(), "BUSY");
        log.info("Ride {} started with driver {}", rideId, ride.getDriverId());
        notificationServiceFeignClient.createNotification(
                NotificationRequest
                        .builder()
                        .recipientId(String.valueOf(ride.getUserId()))
                        .recipientType("USER")
                        .notificationType("RIDE_STARTED")
                        .messageContent("\"RIDE_STARTED\",\n" +
                                "                        \"Your ride is now in progress!\"")
                        .build()
        );
        return mapRideToRideResponse(ride, "Ride started successfully.");
    }

    @Transactional
    public RideResponse completeRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with ID: " + rideId));

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new IllegalStateException("Ride cannot be completed as its status is not IN_PROGRESS. Current status: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride = rideRepository.save(ride);
        // Update driver status in Driver-Service (e.g., to "available")
        if (ride.getDriverId() != null) {
            driverServiceFeignClient.updateDriverStatus(ride.getDriverId(), "ONLINE");
        }
        log.info("Ride {} completed with driver {}", rideId, ride.getDriverId());

        // Trigger Rating-Service request (e.g., via Kafka event or another Feign client)
        // For now, just a notification.
        notificationServiceFeignClient.createNotification(
                NotificationRequest
                        .builder()
                        .recipientId(String.valueOf(ride.getUserId()))
                        .recipientType("USER")
                        .notificationType("RIDE_COMPLETED")
                        .messageContent("Your ride is complete! Please rate your driver.")
                        .build()
        );
        // Also notify the driver to rate the user
        if (ride.getDriverId() != null) {
            notificationServiceFeignClient.createNotification(
                    NotificationRequest
                            .builder()
                            .recipientId(String.valueOf(ride.getDriverId()))
                            .recipientType("DRIVER")
                            .notificationType("RIDE_COMPLETED")
                            .messageContent("Your ride is complete! Please rate your user.")
                            .build()
            );
        }
        return mapRideToRideResponse(ride, "Ride completed successfully. Please provide your rating.");
    }

    @Transactional
    public RideResponse cancelRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with ID: " + rideId));
        if (ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED) {
            throw new IllegalStateException("Ride cannot be cancelled as its status is already " + ride.getStatus());
        }
        // If a driver was reserved, clear the reservation and update driver status
        if (ride.getDriverId() != null && ride.getStatus() == RideStatus.DRIVER_RESERVED) {
            redisTemplate.delete(DRIVER_RESERVATION_KEY_PREFIX + rideId + ":" + ride.getDriverId());
            driverServiceFeignClient.updateDriverStatus(ride.getDriverId(), "ONLINE");
            notificationServiceFeignClient.createNotification(
                    NotificationRequest
                            .builder()
                            .recipientId(String.valueOf(ride.getDriverId()))
                            .recipientType("DRIVER")
                            .notificationType("RIDE_CANCELLED")
                            .messageContent("Your ride request "+rideId+" was cancelled.")
                            .build()
            );
        } else if (ride.getDriverId() != null && ride.getStatus() == RideStatus.CONFIRMED || ride.getStatus() == RideStatus.IN_PROGRESS) {
            // If ride was confirmed or in progress, driver needs to be notified and status updated
            driverServiceFeignClient.updateDriverStatus(ride.getDriverId(), "ONLINE");
            notificationServiceFeignClient.createNotification(
                    NotificationRequest
                            .builder()
                            .recipientId(String.valueOf(ride.getDriverId()))
                            .recipientType("DRIVER")
                            .notificationType("RIDE_CANCELLED")
                            .messageContent("Ride " + rideId + " has been cancelled by the user.")
                            .build()
            );
        }
        ride.setStatus(RideStatus.CANCELLED);
        ride = rideRepository.save(ride);
        log.info("Ride {} cancelled.", rideId);
        notificationServiceFeignClient.createNotification(
                NotificationRequest
                        .builder()
                        .recipientId(String.valueOf(ride.getUserId()))
                        .recipientType("USER")
                        .notificationType("RIDE_CANCELLED")
                        .messageContent("Your ride has been cancelled.")
                        .build()
        );
        return mapRideToRideResponse(ride, "Ride cancelled successfully.");
    }
    // --- Helper methods for driver response logic ---
    // This method would be called if a driver rejects or the 30s timer for acceptance expires.
    private RideResponse handleDriverRejection(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with ID: " + rideId));
        Long rejectedDriverId = ride.getDriverId();
        if (rejectedDriverId != null) {
            driverServiceFeignClient.updateDriverStatus(rejectedDriverId, "ONLINE"); // Make driver available again
            notificationServiceFeignClient.createNotification(
                    NotificationRequest
                            .builder()
                            .recipientId(String.valueOf(ride.getDriverId()))
                            .recipientType("DRIVER")
                            .notificationType("RIDE_CANCELLED")
                            .messageContent("You have rejected ride " + rideId + ".")
                            .build()
            );
        }

        // Unassign driver
        ride.setDriverId(null);
        ride.setStatus(RideStatus.REQUESTED); // Go back to REQUESTED to allow user to select another driver
        ride = rideRepository.save(ride);
        log.info("Driver {} rejected ride {}. Ride status reset to REQUESTED.", rejectedDriverId, rideId);
        // Fetch new nearby drivers for the user to choose from
        List<DriverDto> nextNearbyDrivers = driverServiceFeignClient.getNearbyDrivers(
                getLatitudeFromLocation(ride.getPickupLocation()),
                getLongitudeFromLocation(ride.getPickupLocation()),
                5
        );

        RideResponse response = mapRideToRideResponse(ride);
        response.setNearbyDrivers(nextNearbyDrivers);
        response.setMessage("Driver " + rejectedDriverId + " rejected the ride. Please select another driver or cancel.");
        notificationServiceFeignClient.createNotification(
                NotificationRequest
                        .builder()
                        .recipientId(String.valueOf(ride.getUserId()))
                        .recipientType("USER")
                        .notificationType("DRIVER_REJECTED")
                        .messageContent("Driver " + rejectedDriverId + " rejected your ride. Please select another driver.")
                        .build()
        );
        return response;
    }

    // This method handles the case where the driver response timeout occurs.
    // In a production system, this could be triggered by a background job checking Redis keys.
    // For this example, we're assuming the `handleDriverResponse` catches the `hasKey(false)`
    // and calls this, or a scheduled task would explicitly monitor for expired keys.
    private RideResponse handleDriverTimeout(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with ID: " + rideId));
        Long timedOutDriverId = ride.getDriverId();
        if (timedOutDriverId != null) {
            driverServiceFeignClient.updateDriverStatus(timedOutDriverId, "ONLINE"); // Make driver available again
            // No need to delete Redis key, it expired naturally.
            notificationServiceFeignClient.createNotification(
                    NotificationRequest
                            .builder()
                            .recipientId(String.valueOf(ride.getDriverId()))
                            .recipientType("DRIVER")
                            .notificationType("RIDE_TIMEOUT")
                            .messageContent("You missed the 30-second window to accept ride " + rideId + ".")
                            .build()
            );
        }
        // Unassign driver
        ride.setDriverId(null);
        ride.setStatus(RideStatus.REQUESTED); // Go back to REQUESTED
        ride = rideRepository.save(ride);
        log.info("Driver {} timed out for ride {}. Ride status reset to REQUESTED.", timedOutDriverId, rideId);

        // Fetch new nearby drivers for the user to choose from
        List<DriverDto> nextNearbyDrivers = driverServiceFeignClient.getNearbyDrivers(
                getLatitudeFromLocation(ride.getPickupLocation()),
                getLongitudeFromLocation(ride.getPickupLocation()),
                5
        );

        RideResponse response = mapRideToRideResponse(ride);
        response.setNearbyDrivers(nextNearbyDrivers);
        response.setMessage("Driver " + timedOutDriverId + " did not respond in time. Please select another driver or cancel.");
        notificationServiceFeignClient.createNotification(
                NotificationRequest
                        .builder()
                        .recipientId(String.valueOf(ride.getUserId()))
                        .recipientType("USER")
                        .notificationType("DRIVER_TIMEOUT")
                        .messageContent("The selected driver did not respond in time. Please select another driver.")
                        .build()
        );
        return response;
    }


    private RideResponse mapRideToRideResponse(Ride ride) {
        return mapRideToRideResponse(ride, null);
    }

    private RideResponse mapRideToRideResponse(Ride ride, String message) {
        RideResponse response = new RideResponse();
        response.setId(ride.getId());
        response.setUserId(ride.getUserId());
        response.setDriverId(ride.getDriverId());
        response.setPickupLocation(ride.getPickupLocation());
        response.setDropLocation(ride.getDropLocation());
        response.setFareEstimate(ride.getFareEstimate());
        response.setStatus(ride.getStatus());
        response.setCreatedAt(ride.getCreatedAt());
        response.setUpdatedAt(ride.getUpdatedAt());
        response.setMessage(message);
        return response;
    }

    // Dummy method for deriving coordinates from a location string
    // In a real application, you'd integrate with a Geo-coding service.
    private Double getLatitudeFromLocation(String location) {
        // Simple hash-based dummy for demonstration
        return (double) location.hashCode() % 90;
    }
    private Double getLongitudeFromLocation(String location) {
        // Simple hash-based dummy for demonstration
        return (double) location.hashCode() % 180;
    }
}