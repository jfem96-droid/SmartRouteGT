package com.smartroutegt.backend.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name="driver_reports")
public class DriverReport {
    public enum Type { ACCIDENT, INCIDENT, MECHANICAL }
    public enum Severity { LOW, MEDIUM, HIGH }
    public enum Status { PENDING, UNDER_REVIEW, RESOLVED }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="driver_id") private Driver driver;
    @ManyToOne @JoinColumn(name="trip_id") private Trip trip;
    @ManyToOne @JoinColumn(name="bus_id") private Bus bus;
    @ManyToOne @JoinColumn(name="route_id") private Route route;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Type type;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Severity severity;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.PENDING;
    @Column(nullable=false,length=2000) private String description;
    private Double latitude;
    private Double longitude;
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    private LocalDateTime updatedAt;
    @Column(length=2000) private String resolution;
    public Long getId(){return id;}
    public Driver getDriver(){return driver;} public void setDriver(Driver x){driver=x;}
    public Trip getTrip(){return trip;} public void setTrip(Trip x){trip=x;}
    public Bus getBus(){return bus;} public void setBus(Bus x){bus=x;}
    public Route getRoute(){return route;} public void setRoute(Route x){route=x;}
    public Type getType(){return type;} public void setType(Type x){type=x;}
    public Severity getSeverity(){return severity;} public void setSeverity(Severity x){severity=x;}
    public Status getStatus(){return status;} public void setStatus(Status x){status=x;}
    public String getDescription(){return description;} public void setDescription(String x){description=x;}
    public Double getLatitude(){return latitude;} public void setLatitude(Double x){latitude=x;}
    public Double getLongitude(){return longitude;} public void setLongitude(Double x){longitude=x;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime x){updatedAt=x;}
    public String getResolution(){return resolution;} public void setResolution(String x){resolution=x;}
}
