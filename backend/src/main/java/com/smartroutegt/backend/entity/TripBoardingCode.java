package com.smartroutegt.backend.entity;
import jakarta.persistence.*;
@Entity @Table(name="trip_boarding_code")
public class TripBoardingCode {
 @Id public Long tripId;
 @Column(nullable=false,unique=true,length=80) public String code;
}
