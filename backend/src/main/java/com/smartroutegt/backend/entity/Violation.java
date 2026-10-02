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

@Entity
public class Violation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    private String tipo;
    private String descripcion;
    private Double velocidadRegistrada;
    private Double limitePermitido;
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.PENDING;

    public enum Estado {
        PENDING,
        CONFIRMED,
        REJECTED,
        CANCELLED
    }

    public Violation() {
    }

    public Violation(
            Driver driver,
            Bus bus,
            Trip trip,
            String tipo,
            String descripcion,
            Double velocidadRegistrada,
            Double limitePermitido) {

        this.driver = driver;
        this.bus = bus;
        this.trip = trip;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.velocidadRegistrada = velocidadRegistrada;
        this.limitePermitido = limitePermitido;
        this.fechaHora = LocalDateTime.now();
        this.estado = Estado.PENDING;
    }

    public Long getId() {
        return id;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getVelocidadRegistrada() {
        return velocidadRegistrada;
    }

    public void setVelocidadRegistrada(Double velocidadRegistrada) {
        this.velocidadRegistrada = velocidadRegistrada;
    }

    public Double getLimitePermitido() {
        return limitePermitido;
    }

    public void setLimitePermitido(Double limitePermitido) {
        this.limitePermitido = limitePermitido;
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
}