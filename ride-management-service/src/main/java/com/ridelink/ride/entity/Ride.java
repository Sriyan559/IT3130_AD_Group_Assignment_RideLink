package com.ridelink.ride.entity;

import com.ridelink.ride.domain.RideStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rides")
public class Ride {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false) private Long passengerId;
    private Long driverId;
    @Column(nullable = false, precision = 9, scale = 6) private BigDecimal pickupLatitude;
    @Column(nullable = false, precision = 9, scale = 6) private BigDecimal pickupLongitude;
    @Column(nullable = false, length = 255) private String pickupAddress;
    @Column(nullable = false, precision = 9, scale = 6) private BigDecimal destinationLatitude;
    @Column(nullable = false, precision = 9, scale = 6) private BigDecimal destinationLongitude;
    @Column(nullable = false, length = 255) private String destinationAddress;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RideStatus status;
    @Column(precision = 10, scale = 2) private BigDecimal distanceKm;
    private Long durationMinutes;
    private UUID fareId;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = RideStatus.REQUESTED;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long value) { passengerId = value; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long value) { driverId = value; }
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
    public Instant getUpdatedAt() { return updatedAt; }
}