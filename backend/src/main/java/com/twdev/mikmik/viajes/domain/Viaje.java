package com.twdev.mikmik.viajes.domain;

import com.twdev.mikmik.shared.Coordenada;
import com.twdev.mikmik.shared.Dinero;
import com.twdev.mikmik.viajes.EstadoViaje;
import com.twdev.mikmik.viajes.EventoViaje;
import com.twdev.mikmik.viajes.MetodoPago;
import com.twdev.mikmik.viajes.MotivoCancelacion;
import com.twdev.mikmik.viajes.ReglaDeViajeException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "viaje")
public class Viaje extends AbstractAggregateRoot<Viaje> {

    private static final Set<EstadoViaje> ESTADOS_CAMBIO_DESTINO = Set.of(
        EstadoViaje.CONDUCTOR_ASIGNADO,
        EstadoViaje.CONDUCTOR_EN_PUNTO,
        EstadoViaje.EN_CURSO
    );

    private static final Duration ESPERA_PASAJERO = Duration.ofMinutes(5);

    public record Solicitud(
        UUID pasajeroId,
        Coordenada origen,
        String direccionOrigen,
        Coordenada destino,
        String direccionDestino,
        UUID cotizacionId,
        Dinero precio,
        MetodoPago metodoPago
    ) {
        public Solicitud {
            Objects.requireNonNull(pasajeroId, "pasajeroId");
            Objects.requireNonNull(origen, "origen");
            Objects.requireNonNull(destino, "destino");
            Objects.requireNonNull(cotizacionId, "cotizacionId");
            Objects.requireNonNull(precio, "precio");
            Objects.requireNonNull(metodoPago, "metodoPago");
            exigirTexto(direccionOrigen, "direccionOrigen");
            exigirTexto(direccionDestino, "direccionDestino");

            if (precio.esCero()) {
                throw new IllegalArgumentException("El precio cotizado no puede ser cero");
            }
        }
    }

    @Id
    private UUID id;

    @Version
    private long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoViaje estado;

    @Column(nullable = false)
    private UUID pasajeroId;

    private UUID conductorId;

    private UUID vehiculoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private MetodoPago metodoPago;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "latitud", column = @Column(name = "origen_latitud")),
        @AttributeOverride(name = "longitud", column = @Column(name = "origen_longitud"))
    })
    private Coordenada origen;

    @Column(nullable = false)
    private String direccionOrigen;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "latitud", column = @Column(name = "destino_latitud")),
        @AttributeOverride(name = "longitud", column = @Column(name = "destino_longitud"))
    })
    private Coordenada destino;

    @Column(nullable = false)
    private String direccionDestino;

    @Column(nullable = false)
    private UUID cotizacionId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "monto", column = @Column(name = "precio_cotizado_monto")),
        @AttributeOverride(name = "moneda", column = @Column(name = "precio_cotizado_moneda"))
    })
    private Dinero precioCotizado;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "monto", column = @Column(name = "precio_actual_monto")),
        @AttributeOverride(name = "moneda", column = @Column(name = "precio_actual_moneda"))
    })
    private Dinero precioActual;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MotivoCancelacion motivoCancelacion;

    @Column(nullable = false)
    private int reasignaciones;

    @Column(nullable = false)
    private Instant solicitadoEn;

    private Instant conductorAsignadoEn;
    private Instant conductorEnPuntoEn;
    private Instant iniciadoEn;
    private Instant finalizadoEn;
    private Instant canceladoEn;

    protected Viaje() {
        // Constructor requerido por JPA.
    }

    private Viaje(Solicitud solicitud, Instant ahora) {
        this.id = UUID.randomUUID();
        this.pasajeroId = solicitud.pasajeroId();
        this.origen = solicitud.origen();
        this.direccionOrigen = solicitud.direccionOrigen();
        this.destino = solicitud.destino();
        this.direccionDestino = solicitud.direccionDestino();
        this.cotizacionId = solicitud.cotizacionId();
        this.precioCotizado = solicitud.precio();
        this.precioActual = solicitud.precio();
        this.metodoPago = solicitud.metodoPago();
        this.reasignaciones = 0;
        this.solicitadoEn = ahora;
    }

    public static Viaje solicitar(Solicitud solicitud, Instant ahora) {
        Objects.requireNonNull(solicitud, "solicitud");
        Objects.requireNonNull(ahora, "ahora");

        Viaje viaje = new Viaje(solicitud, ahora);
        if (viaje.metodoPago == MetodoPago.TARJETA) {
            viaje.estado = EstadoViaje.PENDIENTE_PAGO;
            viaje.registerEvent(new EventoViaje.ViajeEnEsperaDePago(
                viaje.id, viaje.pasajeroId, viaje.precioActual, ahora
            ));
        } else {
            viaje.estado = EstadoViaje.BUSCANDO_CONDUCTOR;
            viaje.registrarBusqueda(ahora);
        }
        return viaje;
    }

    public UUID getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public EstadoViaje getEstado() {
        return estado;
    }

    public UUID getPasajeroId() {
        return pasajeroId;
    }

    public UUID getConductorId() {
        return conductorId;
    }

    public UUID getVehiculoId() {
        return vehiculoId;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public Coordenada getOrigen() {
        return origen;
    }

    public String getDireccionOrigen() {
        return direccionOrigen;
    }

    public Coordenada getDestino() {
        return destino;
    }

    public String getDireccionDestino() {
        return direccionDestino;
    }

    public UUID getCotizacionId() {
        return cotizacionId;
    }

    public Dinero getPrecioCotizado() {
        return precioCotizado;
    }

    public Dinero getPrecioActual() {
        return precioActual;
    }

    public MotivoCancelacion getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public int getReasignaciones() {
        return reasignaciones;
    }

    public Instant getSolicitadoEn() {
        return solicitadoEn;
    }

    public Instant getConductorAsignadoEn() {
        return conductorAsignadoEn;
    }

    public Instant getConductorEnPuntoEn() {
        return conductorEnPuntoEn;
    }

    public Instant getIniciadoEn() {
        return iniciadoEn;
    }

    public Instant getFinalizadoEn() {
        return finalizadoEn;
    }

    public Instant getCanceladoEn() {
        return canceladoEn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Viaje otro)) return false;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Viaje.class.hashCode();
    }

    public void confirmarPagoAutorizado(Instant ahora) {
        Objects.requireNonNull(ahora, "ahora");
        transicionar(EstadoViaje.BUSCANDO_CONDUCTOR, EstadoViaje.PENDIENTE_PAGO);
        registrarBusqueda(ahora);
    }

    public void cancelarPorPagoRechazado(Instant ahora) {
        cancelar(MotivoCancelacion.PAGO_RECHAZADO, ahora, EstadoViaje.PENDIENTE_PAGO);
    }

    public void cancelarPorPagoExpirado(Instant ahora) {
        cancelar(MotivoCancelacion.PAGO_EXPIRADO, ahora, EstadoViaje.PENDIENTE_PAGO);
    }

    public void asignarConductor(UUID conductorId, UUID vehiculoId, Instant ahora) {
        Objects.requireNonNull(conductorId, "conductorId");
        Objects.requireNonNull(vehiculoId, "vehiculoId");
        Objects.requireNonNull(ahora, "ahora");
        transicionar(EstadoViaje.CONDUCTOR_ASIGNADO, EstadoViaje.BUSCANDO_CONDUCTOR);

        this.conductorId = conductorId;
        this.vehiculoId = vehiculoId;
        this.conductorAsignadoEn = ahora;
        registerEvent(new EventoViaje.ConductorAsignado(id, pasajeroId, conductorId, vehiculoId, ahora));
    }

    public void marcarSinConductores(Instant ahora) {
        Objects.requireNonNull(ahora, "ahora");
        transicionar(EstadoViaje.SIN_CONDUCTORES, EstadoViaje.BUSCANDO_CONDUCTOR);
        registerEvent(new EventoViaje.ViajeSinConductores(id, pasajeroId, metodoPago, ahora));
    }

    /** El conductor libera el viaje antes de iniciar; el viaje vuelve a búsqueda. */
    public void conductorDesiste(UUID conductorId, Instant ahora) {
        Objects.requireNonNull(ahora, "ahora");
        exigirConductorAsignado(conductorId);
        EstadoViaje previo = estado;
        transicionar(
            EstadoViaje.BUSCANDO_CONDUCTOR,
            EstadoViaje.CONDUCTOR_ASIGNADO,
            EstadoViaje.CONDUCTOR_EN_PUNTO
        );

        this.conductorId = null;
        this.vehiculoId = null;
        this.conductorAsignadoEn = null;
        this.conductorEnPuntoEn = null;
        this.reasignaciones++;
        registerEvent(new EventoViaje.ConductorDesiste(id, conductorId, previo, ahora));
        registrarBusqueda(ahora);
    }

    public void registrarLlegadaConductor(UUID conductorId, Instant ahora) {
        Objects.requireNonNull(ahora, "ahora");
        exigirConductorAsignado(conductorId);
        transicionar(EstadoViaje.CONDUCTOR_EN_PUNTO, EstadoViaje.CONDUCTOR_ASIGNADO);
        this.conductorEnPuntoEn = ahora;
        registerEvent(new EventoViaje.ConductorLlegoAlOrigen(id, pasajeroId, conductorId, ahora));
    }

    public void iniciar(UUID conductorId, Instant ahora) {
        Objects.requireNonNull(ahora, "ahora");
        exigirConductorAsignado(conductorId);
        transicionar(EstadoViaje.EN_CURSO, EstadoViaje.CONDUCTOR_EN_PUNTO);
        this.iniciadoEn = ahora;
        registerEvent(new EventoViaje.ViajeIniciado(id, pasajeroId, conductorId, ahora));
    }

    public void finalizar(UUID conductorId, Instant ahora) {
        Objects.requireNonNull(ahora, "ahora");
        exigirConductorAsignado(conductorId);
        transicionar(EstadoViaje.FINALIZADO, EstadoViaje.EN_CURSO);
        this.finalizadoEn = ahora;
        registerEvent(new EventoViaje.ViajeFinalizado(
            id, pasajeroId, conductorId, precioActual, metodoPago, ahora
        ));
    }

    public void cambiarDestino(
        UUID pasajeroId,
        Coordenada nuevoDestino,
        String nuevaDireccion,
        Dinero nuevoPrecio,
        Instant ahora
    ) {
        Objects.requireNonNull(ahora, "ahora");
        exigirPasajero(pasajeroId);
        Objects.requireNonNull(nuevoDestino, "nuevoDestino");
        Objects.requireNonNull(nuevoPrecio, "nuevoPrecio");
        exigirTexto(nuevaDireccion, "nuevaDireccion");

        if (!ESTADOS_CAMBIO_DESTINO.contains(estado)) {
            throw ReglaDeViajeException.operacionNoPermitida(id, estado, "cambiar el destino");
        }

        Dinero precioAnterior = precioActual;
        this.destino = nuevoDestino;
        this.direccionDestino = nuevaDireccion;
        this.precioActual = nuevoPrecio;
        registerEvent(new EventoViaje.DestinoCambiado(
            id, nuevoDestino, nuevaDireccion, precioAnterior, nuevoPrecio, ahora
        ));
    }

    public void ajustarPrecioPorDesvio(Dinero nuevoPrecio, Instant ahora) {
        Objects.requireNonNull(ahora, "ahora");
        Objects.requireNonNull(nuevoPrecio, "nuevoPrecio");
        if (estado != EstadoViaje.EN_CURSO) {
            throw ReglaDeViajeException.operacionNoPermitida(
                id, estado, "ajustar el precio por desvio"
            );
        }

        Dinero precioAnterior = precioActual;
        this.precioActual = nuevoPrecio;
        registerEvent(new EventoViaje.PrecioActualizadoPorDesvio(
            id, precioAnterior, nuevoPrecio, ahora
        ));
    }

    public void cancelarPorPasajero(UUID pasajeroId, Instant ahora) {
        exigirPasajero(pasajeroId);
        cancelar(
            MotivoCancelacion.PASAJERO,
            ahora,
            EstadoViaje.PENDIENTE_PAGO,
            EstadoViaje.BUSCANDO_CONDUCTOR,
            EstadoViaje.CONDUCTOR_ASIGNADO,
            EstadoViaje.CONDUCTOR_EN_PUNTO
        );
    }

    public void cancelarPorAusenciaDePasajero(UUID conductorId, Instant ahora) {
        Objects.requireNonNull(ahora, "ahora");
        exigirConductorAsignado(conductorId);
        if (estado != EstadoViaje.CONDUCTOR_EN_PUNTO) {
            throw ReglaDeViajeException.operacionNoPermitida(
                id, estado, "cancelar por ausencia de pasajero"
            );
        }
        Objects.requireNonNull(conductorEnPuntoEn, "conductorEnPuntoEn");
        if (ahora.isBefore(conductorEnPuntoEn.plus(ESPERA_PASAJERO))) {
            throw ReglaDeViajeException.operacionNoPermitida(
                id, estado, "cancelar por ausencia de pasajero antes de esperar el tiempo minimo"
            );
        }
        cancelar(MotivoCancelacion.PASAJERO_NO_SE_PRESENTO, ahora, EstadoViaje.CONDUCTOR_EN_PUNTO);
    }

    private void registrarBusqueda(Instant ahora) {
        registerEvent(new EventoViaje.ViajeEnBusqueda(
            id, origen, metodoPago, reasignaciones, ahora
        ));
    }

    private void transicionar(EstadoViaje destino, EstadoViaje... estadosPermitidos) {
        Objects.requireNonNull(destino, "destino");
        if (estado == null
            || Arrays.stream(estadosPermitidos).noneMatch(estado::equals)
            || !estado.puedeTransicionarA(destino)) {
            throw ReglaDeViajeException.transicionInvalida(id, estado, destino);
        }
        estado = destino;
    }

    private void cancelar(
        MotivoCancelacion motivo,
        Instant ahora,
        EstadoViaje... estadosPermitidos
    ) {
        Objects.requireNonNull(motivo, "motivo");
        Objects.requireNonNull(ahora, "ahora");
        EstadoViaje estadoPrevio = estado;
        transicionar(EstadoViaje.CANCELADO, estadosPermitidos);
        this.motivoCancelacion = motivo;
        this.canceladoEn = ahora;
        registerEvent(new EventoViaje.ViajeCancelado(
            id,
            pasajeroId,
            conductorId,
            estadoPrevio,
            motivo,
            metodoPago,
            conductorAsignadoEn,
            ahora
        ));
    }

    private void exigirConductorAsignado(UUID conductorId) {
        Objects.requireNonNull(conductorId, "conductorId");
        if (!conductorId.equals(this.conductorId)) {
            throw ReglaDeViajeException.actorNoAutorizado(
                id, "el conductor no está asignado a este viaje"
            );
        }
    }

    private void exigirPasajero(UUID pasajeroId) {
        Objects.requireNonNull(pasajeroId, "pasajeroId");
        if (!pasajeroId.equals(this.pasajeroId)) {
            throw ReglaDeViajeException.actorNoAutorizado(
                id, "el pasajero no es propietario de este viaje"
            );
        }
    }

    private static void exigirTexto(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombre + " no puede estar vacío");
        }
    }


}
