package com.twdev.mikmik.viajes;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum EstadoViaje {

    // Solo viajes con tarjeta espera la confirmación de pago antes de buscar conductor.
    PENDIENTE_PAGO,
    BUSCANDO_CONDUCTOR,
    // Un conductor aceptó y va en camino al punto de origen.
    CONDUCTOR_ASIGNADO,
    CONDUCTOR_EN_PUNTO,
    EN_CURSO,
    FINALIZADO,
    CANCELADO,
    SIN_CONDUCTORES;

    private Set<EstadoViaje> siguientes;

    static {
        PENDIENTE_PAGO.siguientes     = EnumSet.of(BUSCANDO_CONDUCTOR, CANCELADO);
        BUSCANDO_CONDUCTOR.siguientes = EnumSet.of(CONDUCTOR_ASIGNADO, SIN_CONDUCTORES, CANCELADO);
        CONDUCTOR_ASIGNADO.siguientes = EnumSet.of(CONDUCTOR_EN_PUNTO, BUSCANDO_CONDUCTOR, CANCELADO);
        CONDUCTOR_EN_PUNTO.siguientes = EnumSet.of(EN_CURSO, BUSCANDO_CONDUCTOR, CANCELADO);
        EN_CURSO.siguientes           = EnumSet.of(FINALIZADO);
        FINALIZADO.siguientes         = EnumSet.noneOf(EstadoViaje.class);
        CANCELADO.siguientes          = EnumSet.noneOf(EstadoViaje.class);
        SIN_CONDUCTORES.siguientes    = EnumSet.noneOf(EstadoViaje.class);

    }

    public boolean puedeTransicionarA(EstadoViaje destino) {
        return siguientes.contains(destino);
    }

    public boolean esTerminal() {
        return siguientes.isEmpty();
    }

    public Set<EstadoViaje> getSiguientes() {
        return Collections.unmodifiableSet(siguientes);
    }
}
