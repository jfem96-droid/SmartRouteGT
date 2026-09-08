package com.smartroutegt.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String licencia;
    private String telefono;

    private int sancionesConfirmadas = 0;
    private boolean activo = true;

    public Driver() {
    }

    public Driver(String nombre, String licencia, String telefono) {
        this.nombre = nombre;
        this.licencia = licencia;
        this.telefono = telefono;
        this.sancionesConfirmadas = 0;
        this.activo = true;
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

    public String getLicencia() {
        return licencia;
    }

    public void setLicencia(String licencia) {
        this.licencia = licencia;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getSancionesConfirmadas() {
        return sancionesConfirmadas;
    }

    public void setSancionesConfirmadas(int sancionesConfirmadas) {
        this.sancionesConfirmadas = sancionesConfirmadas;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
