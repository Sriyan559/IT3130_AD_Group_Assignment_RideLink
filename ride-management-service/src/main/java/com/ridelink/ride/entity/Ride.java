package com.ridelink.ride.entity;

import com.ridelink.ride.domain.RideStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a ride in the 'rides' collection of 'ride_db'.
 */
@Document("rides")
public class Ride {
    @org.springframework.data.annotation.Version
    private Long version;
    private String pendingDriverId;
    private boolean releasePending;
    public String getPendingDriverId() { return pendingDriverId; }
    public void setPendingDriverId(String id) { pendingDriverId = id; }
    public boolean isReleasePending() { return releasePending; }
    public void setReleasePending(boolean value) { releasePending = value; }

    @Id
    private UUID id;

    @Indexed
    private String passengerId;

    @Indexed
    private String driverId;

    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;
    private String pickupAddress;
    private BigDecimal destinationLatitude;
    private BigDecimal destinationLongitude;
    private String destinationAddress;

    @Indexed
    private RideStatus status;

    private BigDecimal distanceKm;
    private Long durationMinutes;
    private UUID fareId;
    private Instant createdAt;
    private Instant updatedAt;

    public void prepareForSave() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (status == null) {
            status = RideStatus.REQUESTED;
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String value) { passengerId = value; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String value) { driverId = value; }

    public BigDecimal getPickupLatitude() { return pickupLatitude; }
    public void setPickupLatitude(BigDecimal value) { pickupLatitude = value; }

    public BigDecimal getPickupLongitude() { return pickupLongitude; }
    public void setPickupLongitude(BigDecimal value) { pickupLongitude = value; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String value) { pickupAddress = value; }

    public BigDecimal getDestinationLatitude() { return destinationLatitude; }
    public void setDestinationLatitude(BigDecimal value) { destinationLatitude = value; }

    public BigDecimal getDestinationLongitude() { return destinationLongitude; }
    public void setDestinationLongitude(BigDecimal value) { destinationLongitude = value; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String value) { destinationAddress = value; }

    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus value) { status = value; }

    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal value) { distanceKm = value; }

    public Long getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Long value) { durationMinutes = value; }

    public UUID getFareId() { return fareId; }
    public void setFareId(UUID value) { fareId = value; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
