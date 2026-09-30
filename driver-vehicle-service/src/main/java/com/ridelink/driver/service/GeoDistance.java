package com.ridelink.driver.service;

/** Great-circle distance; coordinates are degrees and the result is kilometres. */
public final class GeoDistance {
    private static final double EARTH_RADIUS_KM = 6371.0088;

    private GeoDistance() {
    }

    public static double kilometres(double lat1, double lng1, double lat2, double lng2) {
        double latitudeDelta = Math.toRadians(lat2 - lat1);
        double longitudeDelta = Math.toRadians(lng2 - lng1);
        double a = Math.pow(Math.sin(latitudeDelta / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(longitudeDelta / 2), 2);
        // Clamp round-off near coincident/antipodal points.
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(Math.max(0, Math.min(1, a))));
    }
}
