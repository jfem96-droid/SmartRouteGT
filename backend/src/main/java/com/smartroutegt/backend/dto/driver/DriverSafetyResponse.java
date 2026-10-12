package com.smartroutegt.backend.dto.driver;

import com.smartroutegt.backend.entity.BusLocation;

public class DriverSafetyResponse {

    private BusLocation location;
    private SpeedStatus speed;
    private StopStatus stop;

    public DriverSafetyResponse(
            BusLocation location,
            SpeedStatus speed,
            StopStatus stop) {
        this.location = location;
        this.speed = speed;
        this.stop = stop;
    }

    public BusLocation getLocation() {
        return location;
    }

    public SpeedStatus getSpeed() {
        return speed;
    }

    public StopStatus getStop() {
        return stop;
    }

    public static class SpeedStatus {

        private double currentKmh;
        private double limitKmh;
        private boolean warning;
        private boolean violationCreated;

        public SpeedStatus(
                double currentKmh,
                double limitKmh,
                boolean warning,
                boolean violationCreated) {
            this.currentKmh = currentKmh;
            this.limitKmh = limitKmh;
            this.warning = warning;
            this.violationCreated = violationCreated;
        }

        public double getCurrentKmh() {
            return currentKmh;
        }

        public double getLimitKmh() {
            return limitKmh;
        }

        public boolean isWarning() {
            return warning;
        }

        public boolean isViolationCreated() {
            return violationCreated;
        }
    }

    public static class StopStatus {

        private Long stopId;
        private String stopName;
        private String status;
        private Double distanceMeters;
        private Integer requiredSeconds;

        public StopStatus(
                Long stopId,
                String stopName,
                String status,
                Double distanceMeters,
                Integer requiredSeconds) {
            this.stopId = stopId;
            this.stopName = stopName;
            this.status = status;
            this.distanceMeters = distanceMeters;
            this.requiredSeconds = requiredSeconds;
        }

        public Long getStopId() {
            return stopId;
        }

        public String getStopName() {
            return stopName;
        }

        public String getStatus() {
            return status;
        }

        public Double getDistanceMeters() {
            return distanceMeters;
        }

        public Integer getRequiredSeconds() {
            return requiredSeconds;
        }
    }
}
