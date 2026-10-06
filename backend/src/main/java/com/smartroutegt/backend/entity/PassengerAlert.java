package com.smartroutegt.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "passenger_alert")
public class PassengerAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    private String descripcion;

    private Double latitud;

    private Double longitud;

    private Double busLatitud;

    private Double busLongitud;

    private Long passengerId;

    private String passengerNombre;

    private String passengerEmail;

    private String passengerTelefono;

    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "bus_id")
    private Bus bus;

    public enum Estado {
        PENDING,
        REVIEWED,
        RESOLVED
    }

    public enum Tipo {
        SOS,
        DRIVER_BEHAVIOR,
        ROUTE_PROBLEM,
        OTHER
    }

    public PassengerAlert() {
        this.fechaHora = LocalDateTime.now();
        this.estado = Estado.PENDING;
    }

    public Long getId() {
        return id;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Double getBusLatitud() {
        return busLatitud;
    }

    public void setBusLatitud(Double busLatitud) {
        this.busLatitud = busLatitud;
    }

    public Double getBusLongitud() {
        return busLongitud;
    }

    public void setBusLongitud(Double busLongitud) {
        this.busLongitud = busLongitud;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public String getPassengerNombre() {
        return passengerNombre;
    }

    public void setPassengerNombre(String passengerNombre) {
        this.passengerNombre = passengerNombre;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public String getPassengerTelefono() {
        return passengerTelefono;
    }

    public void setPassengerTelefono(String passengerTelefono) {
        this.passengerTelefono = passengerTelefono;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }
}