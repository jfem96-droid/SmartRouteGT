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

@Entity
public class DriverRouteAssignment {

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

    private boolean activa = true;

    public enum TipoRecorrido { SOLO_IDA, SOLO_VUELTA, IDA_Y_VUELTA }

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recorrido", nullable = false, length = 20)
    private TipoRecorrido tipoRecorrido = TipoRecorrido.SOLO_IDA;

    public TipoRecorrido getTipoRecorrido() { return tipoRecorrido == null ? TipoRecorrido.SOLO_IDA : tipoRecorrido; }
    public void setTipoRecorrido(TipoRecorrido tipoRecorrido) { this.tipoRecorrido = tipoRecorrido; }


    public DriverRouteAssignment() {
    }

    public DriverRouteAssignment(
            Driver driver,
            Bus bus,
            Route route) {

        this.driver = driver;
        this.bus = bus;
        this.route = route;
        this.activa = true;
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

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}