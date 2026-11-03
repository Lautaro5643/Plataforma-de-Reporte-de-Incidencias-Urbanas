package com.plataforma.incidencias.entity;

/**
 * Ciclo de vida de un reporte. Las transiciones permitidas son:
 * <pre>
 * CREADO     -> ASIGNADO, RECHAZADO
 * ASIGNADO   -> EN_PROCESO, RECHAZADO
 * EN_PROCESO -> RESUELTO, ASIGNADO
 * RESUELTO   -> CERRADO, EN_PROCESO (reapertura)
 * CERRADO, RECHAZADO: estados finales
 * </pre>
 */
public enum EstadoReporte {
    CREADO,
    ASIGNADO,
    EN_PROCESO,
    RESUELTO,
    CERRADO,
    RECHAZADO;

    public boolean puedePasarA(EstadoReporte destino) {
        return switch (this) {
            case CREADO -> destino == ASIGNADO || destino == RECHAZADO;
            case ASIGNADO -> destino == EN_PROCESO || destino == RECHAZADO;
            case EN_PROCESO -> destino == RESUELTO || destino == ASIGNADO;
            case RESUELTO -> destino == CERRADO || destino == EN_PROCESO;
            case CERRADO, RECHAZADO -> false;
        };
    }

    /** Un reporte en estado final ya no admite cambios. */
    public boolean esFinal() {
        return this == CERRADO || this == RECHAZADO;
    }
}
