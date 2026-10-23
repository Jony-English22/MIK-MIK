package com.twdev.mikmik.viajes;

public enum MotivoCancelacion {
    /** El pasajero canceló antes de que iniciara el viaje. */
    PASAJERO,
    /** El conductor esperó en el punto de origen y el pasajero no se presentó. */
    PASAJERO_NO_SE_PRESENTO,
    /** La preautorización de la tarjeta fue rechazada. */
    PAGO_RECHAZADO,
    /** El pago no se confirmó dentro del tiempo límite. */
    PAGO_EXPIRADO,
    /** Cancelado por soporte u operación (fraude, disputa, cuenta suspendida). */
    SOPORTE,
    /** Cancelado por un incidente de seguridad reportado por pasajero o conductor. */
    INCIDENTE_SEGURIDAD
}
