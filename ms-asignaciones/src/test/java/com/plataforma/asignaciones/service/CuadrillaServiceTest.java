package com.plataforma.asignaciones.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.plataforma.asignaciones.dto.CuadrillaRequestDTO;
import com.plataforma.asignaciones.dto.CuadrillaResponseDTO;
import com.plataforma.asignaciones.entity.Cuadrilla;
import com.plataforma.asignaciones.entity.Especialidad;
import com.plataforma.asignaciones.entity.EstadoCuadrilla;
import com.plataforma.asignaciones.mapper.CuadrillaMapper;
import com.plataforma.asignaciones.repository.CuadrillaRepository;
import com.plataforma.common.event.DomainEvent;
import com.plataforma.common.event.EventPublisher;
import com.plataforma.common.exception.BadRequestException;
import com.plataforma.common.exception.ConflictException;
import com.plataforma.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CuadrillaServiceTest {

    @Mock
    private CuadrillaRepository repository;

    @Mock
    private EventPublisher publisher;

    private CuadrillaService service;

    @BeforeEach
    void setUp() {
        service = new CuadrillaService(repository, new CuadrillaMapper(), publisher);
    }

    private Cuadrilla cuadrilla(Long id, String nombre, Especialidad especialidad, EstadoCuadrilla estado, int activos) {
        Cuadrilla c = new Cuadrilla();
        c.setId(id);
        c.setNombre(nombre);
        c.setEspecialidad(especialidad);
        c.setIdZona(10L);
        c.setEstado(estado);
        c.setReportesActivos(activos);
        return c;
    }

    @Test
    void crear_sinEstado_quedaDisponibleConCeroReportes() {
        CuadrillaRequestDTO dto = new CuadrillaRequestDTO("Bacheo Norte", Especialidad.BACHEO, 10L, null);
        when(repository.existsByNombre("Bacheo Norte")).thenReturn(false);
        when(repository.save(any(Cuadrilla.class))).thenAnswer(inv -> {
            Cuadrilla c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CuadrillaResponseDTO respuesta = service.crear(dto);

        assertEquals(1L, respuesta.id());
        assertEquals(EstadoCuadrilla.DISPONIBLE, respuesta.estado());
        assertEquals(0, respuesta.reportesActivos());
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher).publish(eq("asignaciones-eventos"), eq("1"), captor.capture());
        assertEquals("cuadrilla.creada", captor.getValue().eventType());
    }

    @Test
    void crear_nombreDuplicado_lanzaConflictException() {
        CuadrillaRequestDTO dto = new CuadrillaRequestDTO("Bacheo Norte", Especialidad.BACHEO, 10L, null);
        when(repository.existsByNombre("Bacheo Norte")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.crear(dto));

        verify(repository, never()).save(any(Cuadrilla.class));
        verifyNoInteractions(publisher);
    }

    @Test
    void obtenerPorId_inexistente_lanzaResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtenerPorId(99L));
    }

    @Test
    void listar_sinFiltros_devuelveTodas() {
        when(repository.findAll()).thenReturn(List.of(
                cuadrilla(1L, "A", Especialidad.BACHEO, EstadoCuadrilla.DISPONIBLE, 0),
                cuadrilla(2L, "B", Especialidad.GENERAL, EstadoCuadrilla.OCUPADA, 5)));

        assertEquals(2, service.listar(null, null).size());
    }

    @Test
    void listar_conZonaYEstado_usaElFiltroCombinado() {
        when(repository.findByIdZonaAndEstado(10L, EstadoCuadrilla.DISPONIBLE)).thenReturn(List.of(
                cuadrilla(1L, "A", Especialidad.BACHEO, EstadoCuadrilla.DISPONIBLE, 0)));

        assertEquals(1, service.listar(10L, EstadoCuadrilla.DISPONIBLE).size());
        verify(repository, never()).findAll();
    }

    @Test
    void sugerirCuadrilla_prefiereEspecialidadExactaSobreGeneral() {
        when(repository.findByIdZonaAndEstado(10L, EstadoCuadrilla.DISPONIBLE)).thenReturn(List.of(
                cuadrilla(1L, "General", Especialidad.GENERAL, EstadoCuadrilla.DISPONIBLE, 0),
                cuadrilla(2L, "Bacheo", Especialidad.BACHEO, EstadoCuadrilla.DISPONIBLE, 3),
                cuadrilla(3L, "Alumbrado", Especialidad.ALUMBRADO, EstadoCuadrilla.DISPONIBLE, 0)));

        CuadrillaResponseDTO sugerida = service.sugerirCuadrilla(10L, Especialidad.BACHEO);

        assertEquals(2L, sugerida.id());
    }

    @Test
    void sugerirCuadrilla_conMismaEspecialidad_eligeLaDeMenosCarga() {
        when(repository.findByIdZonaAndEstado(10L, EstadoCuadrilla.DISPONIBLE)).thenReturn(List.of(
                cuadrilla(1L, "Bacheo 1", Especialidad.BACHEO, EstadoCuadrilla.DISPONIBLE, 4),
                cuadrilla(2L, "Bacheo 2", Especialidad.BACHEO, EstadoCuadrilla.DISPONIBLE, 1)));

        CuadrillaResponseDTO sugerida = service.sugerirCuadrilla(10L, Especialidad.BACHEO);

        assertEquals(2L, sugerida.id());
    }

    @Test
    void sugerirCuadrilla_sinCandidatas_lanzaResourceNotFoundException() {
        when(repository.findByIdZonaAndEstado(10L, EstadoCuadrilla.DISPONIBLE)).thenReturn(List.of(
                cuadrilla(3L, "Alumbrado", Especialidad.ALUMBRADO, EstadoCuadrilla.DISPONIBLE, 0)));

        assertThrows(ResourceNotFoundException.class, () -> service.sugerirCuadrilla(10L, Especialidad.BACHEO));
    }

    @Test
    void asignarReporte_incrementaReportesActivosYPublicaEvento() {
        Cuadrilla c = cuadrilla(1L, "A", Especialidad.BACHEO, EstadoCuadrilla.DISPONIBLE, 1);
        when(repository.findById(1L)).thenReturn(Optional.of(c));
        when(repository.save(any(Cuadrilla.class))).thenAnswer(inv -> inv.getArgument(0));

        CuadrillaResponseDTO respuesta = service.asignarReporte(1L);

        assertEquals(2, respuesta.reportesActivos());
        assertEquals(EstadoCuadrilla.DISPONIBLE, respuesta.estado());
        verify(publisher).publish(eq("asignaciones-eventos"), eq("1"), any(DomainEvent.class));
    }

    @Test
    void asignarReporte_alLlegarAlMaximo_pasaAOcupada() {
        Cuadrilla c = cuadrilla(1L, "A", Especialidad.BACHEO, EstadoCuadrilla.DISPONIBLE,
                CuadrillaService.MAX_REPORTES_ACTIVOS - 1);
        when(repository.findById(1L)).thenReturn(Optional.of(c));
        when(repository.save(any(Cuadrilla.class))).thenAnswer(inv -> inv.getArgument(0));

        CuadrillaResponseDTO respuesta = service.asignarReporte(1L);

        assertEquals(CuadrillaService.MAX_REPORTES_ACTIVOS, respuesta.reportesActivos());
        assertEquals(EstadoCuadrilla.OCUPADA, respuesta.estado());
    }

    @Test
    void asignarReporte_cuadrillaNoDisponible_lanzaBadRequestException() {
        Cuadrilla c = cuadrilla(1L, "A", Especialidad.BACHEO, EstadoCuadrilla.INACTIVA, 0);
        when(repository.findById(1L)).thenReturn(Optional.of(c));

        assertThrows(BadRequestException.class, () -> service.asignarReporte(1L));

        verify(repository, never()).save(any(Cuadrilla.class));
    }

    @Test
    void liberarReporte_desdeOcupada_vuelveADisponible() {
        Cuadrilla c = cuadrilla(1L, "A", Especialidad.BACHEO, EstadoCuadrilla.OCUPADA,
                CuadrillaService.MAX_REPORTES_ACTIVOS);
        when(repository.findById(1L)).thenReturn(Optional.of(c));
        when(repository.save(any(Cuadrilla.class))).thenAnswer(inv -> inv.getArgument(0));

        CuadrillaResponseDTO respuesta = service.liberarReporte(1L);

        assertEquals(CuadrillaService.MAX_REPORTES_ACTIVOS - 1, respuesta.reportesActivos());
        assertEquals(EstadoCuadrilla.DISPONIBLE, respuesta.estado());
    }

    @Test
    void liberarReporte_sinReportesActivos_lanzaBadRequestException() {
        Cuadrilla c = cuadrilla(1L, "A", Especialidad.BACHEO, EstadoCuadrilla.DISPONIBLE, 0);
        when(repository.findById(1L)).thenReturn(Optional.of(c));

        assertThrows(BadRequestException.class, () -> service.liberarReporte(1L));

        verify(repository, never()).save(any(Cuadrilla.class));
    }

    @Test
    void actualizar_conEstadoNulo_conservaElEstadoActual() {
        Cuadrilla c = cuadrilla(1L, "A", Especialidad.BACHEO, EstadoCuadrilla.OCUPADA, 5);
        when(repository.findById(1L)).thenReturn(Optional.of(c));
        when(repository.existsByNombreAndIdNot("A2", 1L)).thenReturn(false);
        when(repository.save(any(Cuadrilla.class))).thenAnswer(inv -> inv.getArgument(0));

        CuadrillaResponseDTO respuesta = service.actualizar(1L,
                new CuadrillaRequestDTO("A2", Especialidad.GENERAL, 11L, null));

        assertEquals("A2", respuesta.nombre());
        assertEquals(Especialidad.GENERAL, respuesta.especialidad());
        assertEquals(11L, respuesta.idZona());
        assertEquals(EstadoCuadrilla.OCUPADA, respuesta.estado());
        assertEquals(5, respuesta.reportesActivos());
    }

    @Test
    void eliminar_existente_borraYPublicaEvento() {
        Cuadrilla c = cuadrilla(1L, "A", Especialidad.BACHEO, EstadoCuadrilla.DISPONIBLE, 0);
        when(repository.findById(1L)).thenReturn(Optional.of(c));

        service.eliminar(1L);

        verify(repository).delete(c);
        verify(publisher).publish(eq("asignaciones-eventos"), eq("1"), any(DomainEvent.class));
    }
}
