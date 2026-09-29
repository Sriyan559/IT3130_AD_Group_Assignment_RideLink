package com.ridelink.driver.unit;

import com.ridelink.driver.service.GeoDistance;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GeoDistanceTest {
    @Test
    void identicalCoordinatesHaveZeroDistance() {
        assertThat(GeoDistance.kilometres(6.9271, 79.8612, 6.9271, 79.8612)).isZero();
    }

    @Test
    void oneDegreeOnEquatorIsAbout111Kilometres() {
        assertThat(GeoDistance.kilometres(0, 0, 0, 1)).isCloseTo(111.195, within(0.001));
    }

    @Test
    void crossesDateLineByShortestRoute() {
        assertThat(GeoDistance.kilometres(0, 179.9, 0, -179.9)).isCloseTo(22.239, within(0.001));
    }

    @Test
    void polesAndAntipodesRemainFinite() {
        assertThat(GeoDistance.kilometres(90, 0, 90, 180)).isCloseTo(0, within(0.000001));
        assertThat(GeoDistance.kilometres(0, 0, 0, 180)).isCloseTo(20015.114, within(0.001));
    }
}
