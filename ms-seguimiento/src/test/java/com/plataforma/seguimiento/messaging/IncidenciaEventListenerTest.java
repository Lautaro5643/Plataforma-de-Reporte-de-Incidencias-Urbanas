package com.plataforma.seguimiento.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.plataforma.common.event.DomainEvent;
import com.plataforma.seguimiento.dto.HistorialRequestDTO;
import com.plataforma.seguimiento.service.HistorialService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncidenciaEventListenerTest {

    @Mock
    private HistorialService historialService;

    private IncidenciaEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new IncidenciaEventListener(historialService);
    }

    @Test
    void incidenciaCreada_registraLaPrimeraEntradaDelHistorial() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reporteId", 10);
        payload.put("estado", "CREADO");

        listener.onIncidenciaEvent(DomainEvent.of("incidencia.creada", payload));

        ArgumentCaptor<HistorialRequestDTO> captor = ArgumentCaptor.forClass(HistorialRequestDTO.class);
        verify(historialService).registrar(captor.capture());
        HistorialRequestDTO dto = captor.getValue();
        assertEquals(10L, dto.idReporte());
        assertNull(dto.estadoAnterior());
        assertEquals("CREADO", dto.estadoNuevo());
        assertEquals("sistema", dto.usuarioResponsable());
    }

    @Test
    void estadoCambiado_registraElCambioConSuResponsable() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reporteId", 10L);
        payload.put("estadoAnterior", "ASIGNADO");
        payload.put("estadoNuevo", "EN_PROCESO");
        payload.put("observaciones", "La cuadrilla llegó");
        payload.put("usuarioResponsable", "operador1");

        listener.onIncidenciaEvent(DomainEvent.of("incidencia.estado-cambiado", payload));

        ArgumentCaptor<HistorialRequestDTO> captor = ArgumentCaptor.forClass(HistorialRequestDTO.class);
        verify(historialService).registrar(captor.capture());
        HistorialRequestDTO dto = captor.getValue();
        assertEquals(10L, dto.idReporte());
        assertEquals("ASIGNADO", dto.estadoAnterior());
        assertEquals("EN_PROCESO", dto.estadoNuevo());
        assertEquals("La cuadrilla llegó", dto.observaciones());
        assertEquals("operador1", dto.usuarioResponsable());
    }

    @Test
    void estadoCambiado_sinResponsable_usaUsuarioSistema() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reporteId", 10L);
        payload.put("estadoAnterior", "CREADO");
        payload.put("estadoNuevo", "ASIGNADO");
        payload.put("usuarioResponsable", null);

        listener.onIncidenciaEvent(DomainEvent.of("incidencia.estado-cambiado", payload));

        ArgumentCaptor<HistorialRequestDTO> captor = ArgumentCaptor.forClass(HistorialRequestDTO.class);
        verify(historialService).registrar(captor.capture());
        assertEquals("sistema", captor.getValue().usuarioResponsable());
    }

    @Test
    void otroTipoDeEvento_seIgnora() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reporteId", 10L);

        listener.onIncidenciaEvent(DomainEvent.of("incidencia.cuadrilla-asignada", payload));

        verifyNoInteractions(historialService);
    }

    @Test
    void eventoSinReporteId_seIgnora() {
        listener.onIncidenciaEvent(DomainEvent.of("incidencia.creada", new HashMap<>()));

        verify(historialService, never()).registrar(any(HistorialRequestDTO.class));
    }

    @Test
    void eventoNulo_seIgnora() {
        listener.onIncidenciaEvent(null);

        verifyNoInteractions(historialService);
    }
}
