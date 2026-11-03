package com.plataforma.incidencias.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class EstadoReporteTest {

    @ParameterizedTest(name = "{0} -> {1} está permitido")
    @CsvSource({
            "CREADO, ASIGNADO",
            "CREADO, RECHAZADO",
            "ASIGNADO, EN_PROCESO",
            "ASIGNADO, RECHAZADO",
            "EN_PROCESO, RESUELTO",
            "EN_PROCESO, ASIGNADO",
            "RESUELTO, CERRADO",
            "RESUELTO, EN_PROCESO"
    })
    void transicionesPermitidas(EstadoReporte origen, EstadoReporte destino) {
        assertTrue(origen.puedePasarA(destino));
    }

    @ParameterizedTest(name = "{0} -> {1} está prohibido")
    @CsvSource({
            "CREADO, EN_PROCESO",
            "CREADO, RESUELTO",
            "CREADO, CERRADO",
            "ASIGNADO, RESUELTO",
            "ASIGNADO, CERRADO",
            "EN_PROCESO, CERRADO",
            "EN_PROCESO, RECHAZADO",
            "RESUELTO, ASIGNADO",
            "RESUELTO, RECHAZADO"
    })
    void transicionesProhibidas(EstadoReporte origen, EstadoReporte destino) {
        assertFalse(origen.puedePasarA(destino));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoReporte.class, names = {"CERRADO", "RECHAZADO"})
    void estadosFinales_noPermitenNingunaTransicion(EstadoReporte estadoFinal) {
        assertTrue(estadoFinal.esFinal());
        for (EstadoReporte destino : EstadoReporte.values()) {
            assertFalse(estadoFinal.puedePasarA(destino));
        }
    }

    @ParameterizedTest
    @EnumSource(value = EstadoReporte.class, names = {"CREADO", "ASIGNADO", "EN_PROCESO", "RESUELTO"})
    void estadosAbiertos_noSonFinales(EstadoReporte estado) {
        assertFalse(estado.esFinal());
    }

    @Test
    void ningunEstadoPuedePasarASiMismo() {
        for (EstadoReporte estado : EstadoReporte.values()) {
            assertFalse(estado.puedePasarA(estado));
        }
        assertEquals(6, EstadoReporte.values().length);
    }
}
