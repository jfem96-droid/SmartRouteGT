package com.smartroutegt.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class StopVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    private LocalDateTime fechaEntrada;

    private LocalDateTime fechaCumplimiento;

    private double distanciaMinima;

    private boolean cumplida = false;

    public StopVisit() {
    }

    public StopVisit(
            Trip trip,
            Stop stop,
            LocalDateTime fechaEntrada,
            double distanciaMinima) {

        this.trip = trip;
        this.stop = stop;
        this.fechaEntrada = fechaEntrada;
        this.distanciaMinima = distanciaMinima;
        this.cumplida = false;
    }

    public Long getId() {
        return id;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public Stop getStop() {
        return stop;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    public LocalDateTime getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDateTime fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public LocalDateTime getFechaCumplimiento() {
        return fechaCumplimiento;
    }

    public void setFechaCumplimiento(
            LocalDateTime fechaCumplimiento) {
        this.fechaCumplimiento = fechaCumplimiento;
    }

    public double getDistanciaMinima() {
        return distanciaMinima;
    }

    public void setDistanciaMinima(double distanciaMinima) {
        this.distanciaMinima = distanciaMinima;
    }

    public boolean isCumplida() {
        return cumplida;
    }

    public void setCumplida(boolean cumplida) {
        this.cumplida = cumplida;
    }
}