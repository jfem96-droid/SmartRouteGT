package com.smartroutegt.backend.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="passenger_boarding")
public class PassengerBoarding {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public User passenger;
 @ManyToOne(optional=false) public Trip trip;
 public LocalDateTime boardedAt=LocalDateTime.now();
 public LocalDateTime separatedAt;
 public LocalDateTime lastLocationAt;
 public int consecutiveFar=0;
 public boolean closed=false;
}
