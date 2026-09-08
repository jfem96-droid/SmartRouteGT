package com.smartroutegt.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private double latitud;
    private double longitud;
    private double radioPermitido;
    private boolean activa = true;

    public boolean isActiva() {
    return activa;
}

public void setActiva(boolean activa) {
    this.activa = activa;
}

    public Stop() {
    }

    public Stop(String nombre, double latitud, double longitud, double radioPermitido) {
    this.nombre = nombre;
    this.latitud = latitud;
    this.longitud = longitud;
    this.radioPermitido = radioPermitido;
    this.activa = true;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public double getRadioPermitido() {
        return radioPermitido;
    }

    public void setRadioPermitido(double radioPermitido) {
        this.radioPermitido = radioPermitido;
    }
}