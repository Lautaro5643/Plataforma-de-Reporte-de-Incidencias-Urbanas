package com.plataforma.zonas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.plataforma.common.event.DomainEvent;
import com.plataforma.common.event.EventPublisher;
import com.plataforma.common.exception.ConflictException;
import com.plataforma.common.exception.ResourceNotFoundException;
import com.plataforma.zonas.dto.ZonaRequestDTO;
import com.plataforma.zonas.dto.ZonaResponseDTO;
import com.plataforma.zonas.entity.Zona;
import com.plataforma.zonas.mapper.ZonaMapper;
import com.plataforma.zonas.repository.ZonaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ZonaServiceTest {

    @Mock
    private ZonaRepository repository;

    @Mock
    private EventPublisher publisher;

    private ZonaService service;

    @BeforeEach
    void setUp() {
        service = new ZonaService(repository, new ZonaMapper(), publisher);
    }

    private ZonaRequestDTO request() {
        return new ZonaRequestDTO("Centro", "4000", "Comuna 1", "{\"type\":\"Polygon\"}");
    }

    private Zona entidad(Long id) {
        Zona z = new Zona();
        z.setId(id);
        z.setNombre("Centro");
        z.setCodigoPostal("4000");
        z.setComuna("Comuna 1");
        z.setLimitesGeograficos("{\"type\":\"Polygon\"}");
        return z;
    }

    @Test
    void crear_datosValidos_guardaYPublicaEvento() {
        when(repository.existsByNombre("Centro")).thenReturn(false);
        when(repository.save(any(Zona.class))).thenAnswer(inv -> {
            Zona z = inv.getArgument(0);
            z.setId(7L);
            return z;
        });

        ZonaResponseDTO respuesta = service.crear(request());

        assertEquals(7L, respuesta.id());
        assertEquals("Centro", respuesta.nombre());
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher).publish(eq("zonas-eventos"), eq("7"), captor.capture());
        assertEquals("zona.creada", captor.getValue().eventType());
    }

    @Test
    void crear_nombreDuplicado_lanzaConflictException() {
        when(repository.existsByNombre("Centro")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.crear(request()));

        verify(repository, never()).save(any(Zona.class));
        verifyNoInteractions(publisher);
    }

    @Test
    void obtenerPorId_existente_devuelveDto() {
        when(repository.findById(1L)).thenReturn(Optional.of(entidad(1L)));

        ZonaResponseDTO respuesta = service.obtenerPorId(1L);

        assertEquals("4000", respuesta.codigoPostal());
        assertEquals("Comuna 1", respuesta.comuna());
    }

    @Test
    void obtenerPorId_inexistente_lanzaResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtenerPorId(99L));
    }

    @Test
    void listar_sinFiltro_devuelveTodas() {
        when(repository.findAll()).thenReturn(List.of(entidad(1L), entidad(2L)));

        assertEquals(2, service.listar(null).size());
        verify(repository, never()).findByCodigoPostal(any(String.class));
    }

    @Test
    void listar_conCodigoPostal_filtraPorCodigoPostal() {
        when(repository.findByCodigoPostal("4000")).thenReturn(List.of(entidad(1L)));

        List<ZonaResponseDTO> zonas = service.listar("4000");

        assertEquals(1, zonas.size());
        verify(repository, never()).findAll();
    }

    @Test
    void actualizar_datosValidos_actualizaYPublicaEvento() {
        when(repository.findById(1L)).thenReturn(Optional.of(entidad(1L)));
        when(repository.existsByNombreAndIdNot("Norte", 1L)).thenReturn(false);
        when(repository.save(any(Zona.class))).thenAnswer(inv -> inv.getArgument(0));

        ZonaResponseDTO respuesta = service.actualizar(1L, new ZonaRequestDTO("Norte", "4001", "Comuna 2", null));

        assertEquals("Norte", respuesta.nombre());
        assertEquals("4001", respuesta.codigoPostal());
        verify(publisher).publish(eq("zonas-eventos"), eq("1"), any(DomainEvent.class));
    }

    @Test
    void actualizar_nombreDeOtraZona_lanzaConflictException() {
        when(repository.findById(1L)).thenReturn(Optional.of(entidad(1L)));
        when(repository.existsByNombreAndIdNot("Centro", 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.actualizar(1L, request()));

        verify(repository, never()).save(any(Zona.class));
    }

    @Test
    void eliminar_existente_borraYPublicaEvento() {
        Zona existente = entidad(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.eliminar(1L);

        verify(repository).delete(existente);
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher).publish(eq("zonas-eventos"), eq("1"), captor.capture());
        assertEquals("zona.eliminada", captor.getValue().eventType());
    }

    @Test
    void eliminar_inexistente_lanzaResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.eliminar(99L));

        verify(repository, never()).delete(any(Zona.class));
    }
}
