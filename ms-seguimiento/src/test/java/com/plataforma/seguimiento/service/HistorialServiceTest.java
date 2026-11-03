package com.plataforma.seguimiento.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.plataforma.common.event.DomainEvent;
import com.plataforma.common.event.EventPublisher;
import com.plataforma.common.exception.ResourceNotFoundException;
import com.plataforma.seguimiento.dto.HistorialRequestDTO;
import com.plataforma.seguimiento.dto.HistorialResponseDTO;
import com.plataforma.seguimiento.entity.HistorialSeguimiento;
import com.plataforma.seguimiento.mapper.HistorialMapper;
import com.plataforma.seguimiento.repository.HistorialRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HistorialServiceTest {

    @Mock
    private HistorialRepository repository;

    @Mock
    private EventPublisher publisher;

    private HistorialService service;

    @BeforeEach
    void setUp() {
        service = new HistorialService(repository, new HistorialMapper(), publisher);
    }

    private HistorialSeguimiento entidad(Long id, Long idReporte, String anterior, String nuevo) {
        HistorialSeguimiento h = new HistorialSeguimiento();
        h.setId(id);
        h.setIdReporte(idReporte);
        h.setEstadoAnterior(anterior);
        h.setEstadoNuevo(nuevo);
        h.setFechaCambio(LocalDateTime.of(2026, 10, 31, 12, 0));
        h.setObservaciones("obs");
        h.setUsuarioResponsable("operador1");
        return h;
    }

    @Test
    void registrar_guardaLaEntradaYPublicaEvento() {
        when(repository.save(any(HistorialSeguimiento.class))).thenAnswer(inv -> {
            HistorialSeguimiento h = inv.getArgument(0);
            h.setId(3L);
            return h;
        });

        HistorialResponseDTO respuesta = service.registrar(new HistorialRequestDTO(
                10L, "CREADO", "ASIGNADO", "Se asignó cuadrilla", "operador1", "http://fotos/e1.jpg"));

        assertEquals(3L, respuesta.id());
        assertEquals(10L, respuesta.idReporte());
        assertEquals("CREADO", respuesta.estadoAnterior());
        assertEquals("ASIGNADO", respuesta.estadoNuevo());
        assertEquals("http://fotos/e1.jpg", respuesta.fotoEvidencia());
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher).publish(eq("seguimiento-eventos"), eq("10"), captor.capture());
        assertEquals("seguimiento.registrado", captor.getValue().eventType());
        assertEquals(3L, captor.getValue().payload().get("historialId"));
    }

    @Test
    void obtenerPorId_existente_devuelveDto() {
        when(repository.findById(1L)).thenReturn(Optional.of(entidad(1L, 10L, "CREADO", "ASIGNADO")));

        HistorialResponseDTO respuesta = service.obtenerPorId(1L);

        assertEquals("ASIGNADO", respuesta.estadoNuevo());
    }

    @Test
    void obtenerPorId_inexistente_lanzaResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtenerPorId(99L));
    }

    @Test
    void listar_devuelveTodasLasEntradas() {
        when(repository.findAllByOrderByFechaCambioDesc()).thenReturn(List.of(
                entidad(2L, 10L, "CREADO", "ASIGNADO"), entidad(1L, 10L, null, "CREADO")));

        assertEquals(2, service.listar().size());
    }

    @Test
    void listarPorReporte_devuelveElHistorialDelReporte() {
        when(repository.findByIdReporteOrderByFechaCambioDesc(10L)).thenReturn(List.of(
                entidad(2L, 10L, "CREADO", "ASIGNADO"), entidad(1L, 10L, null, "CREADO")));

        List<HistorialResponseDTO> historial = service.listarPorReporte(10L);

        assertEquals(2, historial.size());
        assertEquals(2L, historial.get(0).id());
    }

    @Test
    void listarPorReporte_sinHistorial_devuelveListaVacia() {
        when(repository.findByIdReporteOrderByFechaCambioDesc(77L)).thenReturn(List.of());

        assertTrue(service.listarPorReporte(77L).isEmpty());
    }
}
