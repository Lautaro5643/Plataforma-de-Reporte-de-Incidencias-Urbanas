package com.plataforma.incidencias.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.plataforma.common.event.DomainEvent;
import com.plataforma.common.event.EventPublisher;
import com.plataforma.common.exception.BadRequestException;
import com.plataforma.common.exception.ResourceNotFoundException;
import com.plataforma.incidencias.dto.AsignarCuadrillaRequestDTO;
import com.plataforma.incidencias.dto.CambioEstadoRequestDTO;
import com.plataforma.incidencias.dto.ReporteRequestDTO;
import com.plataforma.incidencias.dto.ReporteResponseDTO;
import com.plataforma.incidencias.entity.EstadoReporte;
import com.plataforma.incidencias.entity.Prioridad;
import com.plataforma.incidencias.entity.Reporte;
import com.plataforma.incidencias.entity.TipoIncidencia;
import com.plataforma.incidencias.mapper.ReporteMapper;
import com.plataforma.incidencias.repository.ReporteRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository repository;

    @Mock
    private EventPublisher publisher;

    private ReporteService service;

    @BeforeEach
    void setUp() {
        service = new ReporteService(repository, new ReporteMapper(), publisher);
    }

    private ReporteRequestDTO request(Prioridad prioridad) {
        return new ReporteRequestDTO("Bache en Av. Mitre", "Bache profundo frente al 500", TipoIncidencia.BACHE,
                "http://fotos/1.jpg", prioridad, 1L, 2L);
    }

    private Reporte reporte(Long id, EstadoReporte estado, Long idCuadrilla) {
        Reporte r = new Reporte();
        r.setId(id);
        r.setTitulo("Bache en Av. Mitre");
        r.setDescripcion("Bache profundo frente al 500");
        r.setTipoIncidencia(TipoIncidencia.BACHE);
        r.setPrioridad(Prioridad.MEDIA);
        r.setEstadoActual(estado);
        r.setIdCiudadano(1L);
        r.setIdZona(2L);
        r.setIdCuadrilla(idCuadrilla);
        r.setFechaCreacion(LocalDateTime.of(2026, 10, 30, 9, 0));
        return r;
    }

    private void stubGuardarDevuelveElMismo() {
        when(repository.save(any(Reporte.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void crear_sinPrioridad_quedaCreadoConPrioridadMediaYPublicaEvento() {
        when(repository.save(any(Reporte.class))).thenAnswer(inv -> {
            Reporte r = inv.getArgument(0);
            r.setId(5L);
            return r;
        });

        ReporteResponseDTO respuesta = service.crear(request(null));

        assertEquals(5L, respuesta.id());
        assertEquals(EstadoReporte.CREADO, respuesta.estadoActual());
        assertEquals(Prioridad.MEDIA, respuesta.prioridad());
        assertNull(respuesta.idCuadrilla());
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher).publish(eq("incidencias-eventos"), eq("5"), captor.capture());
        DomainEvent evento = captor.getValue();
        assertEquals("incidencia.creada", evento.eventType());
        assertEquals(5L, evento.payload().get("reporteId"));
        assertEquals("CREADO", evento.payload().get("estado"));
        assertEquals(1L, evento.payload().get("idCiudadano"));
        assertEquals(2L, evento.payload().get("idZona"));
    }

    @Test
    void crear_conPrioridadAlta_respetaLaPrioridad() {
        stubGuardarDevuelveElMismo();

        ReporteResponseDTO respuesta = service.crear(request(Prioridad.ALTA));

        assertEquals(Prioridad.ALTA, respuesta.prioridad());
    }

    @Test
    void obtenerPorId_inexistente_lanzaResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtenerPorId(99L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void listar_devuelveLosReportesEncontrados() {
        when(repository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(reporte(1L, EstadoReporte.CREADO, null), reporte(2L, EstadoReporte.CREADO, null)));

        List<ReporteResponseDTO> lista = service.listar(EstadoReporte.CREADO, 2L, null);

        assertEquals(2, lista.size());
        assertEquals(1L, lista.get(0).id());
    }

    @Test
    void actualizar_reporteAbierto_actualizaLosCamposSinTocarElEstado() {
        when(repository.findById(1L)).thenReturn(Optional.of(reporte(1L, EstadoReporte.ASIGNADO, 7L)));
        stubGuardarDevuelveElMismo();

        ReporteResponseDTO respuesta = service.actualizar(1L, new ReporteRequestDTO("Nuevo título", null,
                TipoIncidencia.LUMINARIA, null, Prioridad.CRITICA, 1L, 3L));

        assertEquals("Nuevo título", respuesta.titulo());
        assertEquals(TipoIncidencia.LUMINARIA, respuesta.tipoIncidencia());
        assertEquals(Prioridad.CRITICA, respuesta.prioridad());
        assertEquals(3L, respuesta.idZona());
        assertEquals(EstadoReporte.ASIGNADO, respuesta.estadoActual());
        assertEquals(7L, respuesta.idCuadrilla());
    }

    @Test
    void actualizar_reporteCerrado_lanzaBadRequestException() {
        when(repository.findById(1L)).thenReturn(Optional.of(reporte(1L, EstadoReporte.CERRADO, 7L)));

        assertThrows(BadRequestException.class, () -> service.actualizar(1L, request(null)));

        verify(repository, never()).save(any(Reporte.class));
    }

    @Test
    void cambiarEstado_transicionValida_actualizaYPublicaEvento() {
        when(repository.findById(1L)).thenReturn(Optional.of(reporte(1L, EstadoReporte.ASIGNADO, 7L)));
        stubGuardarDevuelveElMismo();

        ReporteResponseDTO respuesta = service.cambiarEstado(1L,
                new CambioEstadoRequestDTO(EstadoReporte.EN_PROCESO, "La cuadrilla llegó al lugar", "operador1"));

        assertEquals(EstadoReporte.EN_PROCESO, respuesta.estadoActual());
        assertNull(respuesta.fechaCierre());
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher).publish(eq("incidencias-eventos"), eq("1"), captor.capture());
        DomainEvent evento = captor.getValue();
        assertEquals("incidencia.estado-cambiado", evento.eventType());
        assertEquals("ASIGNADO", evento.payload().get("estadoAnterior"));
        assertEquals("EN_PROCESO", evento.payload().get("estadoNuevo"));
        assertEquals("La cuadrilla llegó al lugar", evento.payload().get("observaciones"));
        assertEquals("operador1", evento.payload().get("usuarioResponsable"));
    }

    @Test
    void cambiarEstado_aCerrado_registraLaFechaDeCierre() {
        when(repository.findById(1L)).thenReturn(Optional.of(reporte(1L, EstadoReporte.RESUELTO, 7L)));
        stubGuardarDevuelveElMismo();

        ReporteResponseDTO respuesta = service.cambiarEstado(1L,
                new CambioEstadoRequestDTO(EstadoReporte.CERRADO, null, null));

        assertEquals(EstadoReporte.CERRADO, respuesta.estadoActual());
        assertNotNull(respuesta.fechaCierre());
    }

    @Test
    void cambiarEstado_aRechazado_registraLaFechaDeCierre() {
        when(repository.findById(1L)).thenReturn(Optional.of(reporte(1L, EstadoReporte.CREADO, null)));
        stubGuardarDevuelveElMismo();

        ReporteResponseDTO respuesta = service.cambiarEstado(1L,
                new CambioEstadoRequestDTO(EstadoReporte.RECHAZADO, "Reporte duplicado", "operador1"));

        assertEquals(EstadoReporte.RECHAZADO, respuesta.estadoActual());
        assertNotNull(respuesta.fechaCierre());
    }

    @Test
    void cambiarEstado_transicionInvalida_lanzaBadRequestException() {
        when(repository.findById(1L)).thenReturn(Optional.of(reporte(1L, EstadoReporte.CREADO, null)));

        assertThrows(BadRequestException.class, () -> service.cambiarEstado(1L,
                new CambioEstadoRequestDTO(EstadoReporte.CERRADO, null, null)));

        verify(repository, never()).save(any(Reporte.class));
        verify(publisher, never()).publish(any(String.class), any(String.class), any(DomainEvent.class));
    }

    @Test
    void cambiarEstado_aAsignadoSinCuadrilla_lanzaBadRequestException() {
        when(repository.findById(1L)).thenReturn(Optional.of(reporte(1L, EstadoReporte.CREADO, null)));

        assertThrows(BadRequestException.class, () -> service.cambiarEstado(1L,
                new CambioEstadoRequestDTO(EstadoReporte.ASIGNADO, null, null)));

        verify(repository, never()).save(any(Reporte.class));
    }

    @Test
    void asignarCuadrilla_reporteCreado_pasaAAsignadoYPublicaDosEventos() {
        when(repository.findById(1L)).thenReturn(Optional.of(reporte(1L, EstadoReporte.CREADO, null)));
        stubGuardarDevuelveElMismo();

        ReporteResponseDTO respuesta = service.asignarCuadrilla(1L, new AsignarCuadrillaRequestDTO(7L, "operador1"));

        assertEquals(7L, respuesta.idCuadrilla());
        assertEquals(EstadoReporte.ASIGNADO, respuesta.estadoActual());
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher, times(2)).publish(eq("incidencias-eventos"), eq("1"), captor.capture());
        List<DomainEvent> eventos = captor.getAllValues();
        assertEquals("incidencia.cuadrilla-asignada", eventos.get(0).eventType());
        assertEquals("incidencia.estado-cambiado", eventos.get(1).eventType());
        assertEquals("CREADO", eventos.get(1).payload().get("estadoAnterior"));
        assertEquals("ASIGNADO", eventos.get(1).payload().get("estadoNuevo"));
    }

    @Test
    void asignarCuadrilla_reporteYaAsignado_soloCambiaLaCuadrilla() {
        when(repository.findById(1L)).thenReturn(Optional.of(reporte(1L, EstadoReporte.ASIGNADO, 7L)));
        stubGuardarDevuelveElMismo();

        ReporteResponseDTO respuesta = service.asignarCuadrilla(1L, new AsignarCuadrillaRequestDTO(8L, null));

        assertEquals(8L, respuesta.idCuadrilla());
        assertEquals(EstadoReporte.ASIGNADO, respuesta.estadoActual());
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher, times(1)).publish(eq("incidencias-eventos"), eq("1"), captor.capture());
        assertEquals("incidencia.cuadrilla-asignada", captor.getValue().eventType());
    }

    @Test
    void asignarCuadrilla_reporteEnProceso_lanzaBadRequestException() {
        when(repository.findById(1L)).thenReturn(Optional.of(reporte(1L, EstadoReporte.EN_PROCESO, 7L)));

        assertThrows(BadRequestException.class,
                () -> service.asignarCuadrilla(1L, new AsignarCuadrillaRequestDTO(8L, null)));

        verify(repository, never()).save(any(Reporte.class));
    }

    @Test
    void eliminar_existente_borraElReporte() {
        Reporte existente = reporte(1L, EstadoReporte.CREADO, null);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.eliminar(1L);

        verify(repository).delete(existente);
    }

    @Test
    void eliminar_inexistente_lanzaResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.eliminar(99L));

        verify(repository, never()).delete(any(Reporte.class));
    }
}
