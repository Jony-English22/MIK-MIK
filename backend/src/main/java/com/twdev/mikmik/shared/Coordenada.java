package com.twdev.mikmik.shared;

import jakarta.persistence.Embeddable;

@Embeddable
public record Coordenada(double latitud, double longitud) {

    public Coordenada {
        if (Double.isNaN(latitud) || latitud < -90.0 || latitud > 90.0) {
            throw new IllegalArgumentException("Latitud fuera del rango: " + latitud);
        }
        if (Double.isNaN(longitud) || longitud < -180.0 || longitud > 180.0) {
            throw new IllegalArgumentException("Longitud fuera del rango: " + longitud);
        }
    }
}
