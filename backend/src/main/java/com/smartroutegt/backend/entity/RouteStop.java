package com.smartroutegt.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class RouteStop {

    public enum TipoParada {
        NORMAL,
        ALTA_DEMANDA,
        REGULADORA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    private int orden;

    @Enumerated(EnumType.STRING)
    private TipoParada tipoParada = TipoParada.NORMAL;

    private int tiempoMinimoSegundos = 15;

    private int tiempoObjetivoSegundos = 20;

    private int tiempoMaximoSegundos = 30;

    public RouteStop() {
    }

    public RouteStop(Route route, Stop stop, int orden) {
        this.route = route;
        this.stop = stop;
        this.orden = orden;
        this.tipoParada = TipoParada.NORMAL;
        this.tiempoMinimoSegundos = 15;
        this.tiempoObjetivoSegundos = 20;
        this.tiempoMaximoSegundos = 30;
    }

    public Long getId() {
        return id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Stop getStop() {
        return stop;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public TipoParada getTipoParada() {
        return tipoParada;
    }

    public void setTipoParada(TipoParada tipoParada) {
        this.tipoParada = tipoParada;
    }

    public int getTiempoMinimoSegundos() {
        return tiempoMinimoSegundos;
    }

    public void setTiempoMinimoSegundos(int tiempoMinimoSegundos) {
        this.tiempoMinimoSegundos = tiempoMinimoSegundos;
    }

    public int getTiempoObjetivoSegundos() {
        return tiempoObjetivoSegundos;
    }

    public void setTiempoObjetivoSegundos(int tiempoObjetivoSegundos) {
        this.tiempoObjetivoSegundos = tiempoObjetivoSegundos;
    }

    public int getTiempoMaximoSegundos() {
        return tiempoMaximoSegundos;
    }

    public void setTiempoMaximoSegundos(int tiempoMaximoSegundos) {
        this.tiempoMaximoSegundos = tiempoMaximoSegundos;
    }
}