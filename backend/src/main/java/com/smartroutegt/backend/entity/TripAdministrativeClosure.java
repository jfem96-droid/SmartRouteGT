package com.smartroutegt.backend.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="trip_administrative_closure")
public class TripAdministrativeClosure {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="trip_id",nullable=false) private Long tripId;
 @Column(name="operator_name",nullable=false,length=255) private String operatorName;
 @Column(nullable=false,length=1000) private String reason;
 @Column(name="closed_at",nullable=false) private LocalDateTime closedAt;
 protected TripAdministrativeClosure() {}
 public TripAdministrativeClosure(Long tripId,String operatorName,String reason) {
  this.tripId=tripId;this.operatorName=operatorName;this.reason=reason;this.closedAt=LocalDateTime.now();
 }
 public Long getId(){return id;} public Long getTripId(){return tripId;}
 public String getOperatorName(){return operatorName;} public String getReason(){return reason;}
 public LocalDateTime getClosedAt(){return closedAt;}
}
