package com.smartroutegt.backend.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="passenger_feedback",uniqueConstraints=@UniqueConstraint(columnNames={"boarding_id","kind"}))
public class PassengerFeedback {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) @JoinColumn(name="boarding_id") public PassengerBoarding boarding;
 @ManyToOne(optional=false) public User passenger;
 @ManyToOne(optional=false) public Trip trip;
 @ManyToOne(optional=false) public Driver driver;
 @ManyToOne(optional=false) public Bus bus;
 @Column(nullable=false,length=16) public String kind;
 public Integer stars;
 @Column(length=50) public String reason;
 @Column(length=1000) public String comment;
 public LocalDateTime createdAt=LocalDateTime.now();
}
