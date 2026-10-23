package com.twdev.mikmik.viajes;

import java.util.UUID;

/**
 * Violación de una regla del agregado Viaje. La capa web la traduce a 409 Conflict.
 */
public class ReglaDeViajeException extends RuntimeException {

    private ReglaDeViajeException(String mensaje) {
        super(mensaje);
    }

    public static ReglaDeViajeException transicionInvalida(UUID viajeId, EstadoViaje actual, EstadoViaje destino) {
        return new ReglaDeViajeException(
                "Viaje %s: transición inválida %s -> %s".formatted(viajeId, actual, destino));
    }

    public static ReglaDeViajeException operacionNoPermitida(UUID viajeId, EstadoViaje actual, String operacion) {
        return new ReglaDeViajeException(
                "Viaje %s: no se puede %s en estado %s".formatted(viajeId, operacion, actual));
    }

    public static ReglaDeViajeException actorNoAutorizado(UUID viajeId, String detalle) {
        return new ReglaDeViajeException("Viaje %s: %s".formatted(viajeId, detalle));
    }
}
