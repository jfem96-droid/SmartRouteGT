package com.smartroutegt.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class Trip {

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
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private boolean activo = true;

    public enum Sentido { IDA, VUELTA }

    @Enumerated(EnumType.STRING)
    @Column(name = "sentido_actual", length = 10, nullable = false)
    private Sentido sentidoActual = Sentido.IDA;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recorrido", length = 20, nullable = false)
    private DriverRouteAssignment.TipoRecorrido tipoRecorrido = DriverRouteAssignment.TipoRecorrido.SOLO_IDA;

    public Sentido getSentidoActual() { return sentidoActual == null ? Sentido.IDA : sentidoActual; }
    public void setSentidoActual(Sentido sentidoActual) { this.sentidoActual = sentidoActual; }
    public DriverRouteAssignment.TipoRecorrido getTipoRecorrido() { return tipoRecorrido == null ? DriverRouteAssignment.TipoRecorrido.SOLO_IDA : tipoRecorrido; }
    public void setTipoRecorrido(DriverRouteAssignment.TipoRecorrido tipoRecorrido) { this.tipoRecorrido = tipoRecorrido; }


    public Trip() {
    }

    public Trip(Driver driver, Bus bus, Route route) {
        this.driver = driver;
        this.bus = bus;
        this.route = route;
        this.fechaInicio = LocalDateTime.now();
        this.activo = true;
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

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}