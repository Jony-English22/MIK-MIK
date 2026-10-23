package com.twdev.mikmik.viajes;

import com.twdev.mikmik.shared.Coordenada;
import com.twdev.mikmik.shared.Dinero;

import java.time.Instant;
import java.util.UUID;

public sealed interface EventoViaje {

    UUID viajeId();

    Instant ocurridoEn();

    /** Un viaje con tarjeta espera la confirmación de pago antes de buscar conductor. */
    record ViajeEnEsperaDePago(
        UUID viajeId,
        UUID pasajeroId,
        Dinero precio,
        Instant ocurridoEn
    ) implements EventoViaje {
    }

    /** El viaje está en búsqueda de un conductor. */
    record ViajeEnBusqueda(
        UUID viajeId,
        Coordenada origen,
        MetodoPago metodoPago,
        int reasignaciones,
        Instant ocurridoEn
    ) implements EventoViaje {
    }

    /** Un conductor ha sido asignado al viaje. */
    record ConductorAsignado(
        UUID viajeId,
        UUID pasajeroId,
        UUID conductorId,
        UUID vehiculoId,
        Instant ocurridoEn
    ) implements EventoViaje {
    }

    /** Un conductor ha cancelado el viaje. */
    record ConductorCanceloViaje(
        UUID viajeId,
        UUID conductorId,
        EstadoViaje estadoPrevio,
        Instant ocurridoEn
    ) implements EventoViaje {
    }

    /** No fue posible encontrar un conductor disponible. */
    record ViajeSinConductores(
        UUID viajeId,
        UUID pasajeroId,
        MetodoPago metodoPago,
        Instant ocurridoEn
    ) implements EventoViaje {
    }

    /** El conductor libera el viaje y este vuelve a la búsqueda. */
    record ConductorDesiste(
        UUID viajeId,
        UUID conductorId,
        EstadoViaje estadoPrevio,
        Instant ocurridoEn
    ) implements EventoViaje {
    }

    /** El conductor ha llegado al punto de origen. */
    record ConductorLlegoAlOrigen(
        UUID viajeId,
        UUID pasajeroId,
        UUID conductorId,
        Instant ocurridoEn
    ) implements EventoViaje {
    }

    /** El viaje ha iniciado. */
    record ViajeIniciado(
        UUID viajeId,
        UUID pasajeroId,
        UUID conductorId,
        Instant ocurridoEn
    ) implements EventoViaje {
    }

    /** El destino y el precio del viaje han cambiado. */
    record DestinoCambiado(
        UUID viajeId,
        Coordenada nuevoDestino,
        String nuevaDireccion,
        Dinero precioAnterior,
        Dinero precioNuevo,
        Instant ocurridoEn
    ) implements EventoViaje {
    }

    /** El precio del viaje ha sido actualizado por un desvío. */
    record PrecioActualizadoPorDesvio(
        UUID viajeId,
        Dinero precioAnterior,
        Dinero precioNuevo,
        Instant ocurridoEn
    ) implements EventoViaje {
    }

    /** El viaje ha finalizado. */
    record ViajeFinalizado(
        UUID viajeId,
        UUID pasajeroId,
        UUID conductorId,
        Dinero precioFinal,
        MetodoPago metodoPago,
        Instant ocurridoEn
    ) implements EventoViaje {
    }

    /** Evento emitido cuando un viaje es cancelado por el pasajero o el sistema. */
    record ViajeCancelado(
        UUID viajeId,
        UUID pasajeroId,
        UUID conductorId,
        EstadoViaje estadoPrevio,
        MotivoCancelacion motivo,
        MetodoPago metodoPago,
        Instant conductorAsignadoEn,
        Instant ocurridoEn
    ) implements EventoViaje {
    }
}
