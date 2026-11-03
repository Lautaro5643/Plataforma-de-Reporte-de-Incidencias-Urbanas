package com.plataforma.ciudadanos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.plataforma.ciudadanos.dto.CiudadanoRequestDTO;
import com.plataforma.ciudadanos.dto.CiudadanoResponseDTO;
import com.plataforma.ciudadanos.entity.Ciudadano;
import com.plataforma.ciudadanos.mapper.CiudadanoMapper;
import com.plataforma.ciudadanos.repository.CiudadanoRepository;
import com.plataforma.common.event.DomainEvent;
import com.plataforma.common.event.EventPublisher;
import com.plataforma.common.exception.ConflictException;
import com.plataforma.common.exception.ResourceNotFoundException;
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
class CiudadanoServiceTest {

    @Mock
    private CiudadanoRepository repository;

    @Mock
    private EventPublisher publisher;

    private CiudadanoService service;

    @BeforeEach
    void setUp() {
        service = new CiudadanoService(repository, new CiudadanoMapper(), publisher);
    }

    private CiudadanoRequestDTO request() {
        return new CiudadanoRequestDTO("Juan Pérez", "juan@mail.com", "3814001122", "30123456");
    }

    private Ciudadano entidad(Long id) {
        Ciudadano c = new Ciudadano();
        c.setId(id);
        c.setNombre("Juan Pérez");
        c.setEmail("juan@mail.com");
        c.setTelefono("3814001122");
        c.setDni("30123456");
        c.setFechaRegistro(LocalDateTime.of(2026, 10, 1, 10, 0));
        return c;
    }

    @Test
    void crear_datosValidos_guardaYPublicaEvento() {
        when(repository.existsByEmail("juan@mail.com")).thenReturn(false);
        when(repository.existsByDni("30123456")).thenReturn(false);
        when(repository.save(any(Ciudadano.class))).thenAnswer(inv -> {
            Ciudadano c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CiudadanoResponseDTO respuesta = service.crear(request());

        assertEquals(1L, respuesta.id());
        assertEquals("Juan Pérez", respuesta.nombre());
        assertEquals("juan@mail.com", respuesta.email());
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher).publish(eq("ciudadanos-eventos"), eq("1"), captor.capture());
        assertEquals("ciudadano.creado", captor.getValue().eventType());
    }

    @Test
    void crear_emailDuplicado_lanzaConflictException() {
        when(repository.existsByEmail("juan@mail.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.crear(request()));

        verify(repository, never()).save(any(Ciudadano.class));
        verifyNoInteractions(publisher);
    }

    @Test
    void crear_dniDuplicado_lanzaConflictException() {
        when(repository.existsByEmail("juan@mail.com")).thenReturn(false);
        when(repository.existsByDni("30123456")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.crear(request()));

        verify(repository, never()).save(any(Ciudadano.class));
    }

    @Test
    void obtenerPorId_existente_devuelveDto() {
        when(repository.findById(1L)).thenReturn(Optional.of(entidad(1L)));

        CiudadanoResponseDTO respuesta = service.obtenerPorId(1L);

        assertEquals(1L, respuesta.id());
        assertEquals("30123456", respuesta.dni());
    }

    @Test
    void obtenerPorId_inexistente_lanzaResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtenerPorId(99L));
    }

    @Test
    void obtenerPorDni_inexistente_lanzaResourceNotFoundException() {
        when(repository.findByDni("11111111")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtenerPorDni("11111111"));
    }

    @Test
    void listar_devuelveTodosLosCiudadanos() {
        when(repository.findAll()).thenReturn(List.of(entidad(1L), entidad(2L)));

        List<CiudadanoResponseDTO> lista = service.listar();

        assertEquals(2, lista.size());
    }

    @Test
    void actualizar_datosValidos_actualizaYPublicaEvento() {
        when(repository.findById(1L)).thenReturn(Optional.of(entidad(1L)));
        when(repository.existsByEmailAndIdNot("nuevo@mail.com", 1L)).thenReturn(false);
        when(repository.existsByDniAndIdNot("30123456", 1L)).thenReturn(false);
        when(repository.save(any(Ciudadano.class))).thenAnswer(inv -> inv.getArgument(0));

        CiudadanoResponseDTO respuesta = service.actualizar(1L,
                new CiudadanoRequestDTO("Juan P.", "nuevo@mail.com", "3814009999", "30123456"));

        assertEquals("nuevo@mail.com", respuesta.email());
        assertEquals("Juan P.", respuesta.nombre());
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher).publish(eq("ciudadanos-eventos"), eq("1"), captor.capture());
        assertEquals("ciudadano.actualizado", captor.getValue().eventType());
    }

    @Test
    void actualizar_emailDeOtroCiudadano_lanzaConflictException() {
        when(repository.findById(1L)).thenReturn(Optional.of(entidad(1L)));
        when(repository.existsByEmailAndIdNot("juan@mail.com", 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.actualizar(1L, request()));

        verify(repository, never()).save(any(Ciudadano.class));
    }

    @Test
    void eliminar_existente_borraYPublicaEvento() {
        Ciudadano existente = entidad(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.eliminar(1L);

        verify(repository).delete(existente);
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher).publish(eq("ciudadanos-eventos"), eq("1"), captor.capture());
        assertEquals("ciudadano.eliminado", captor.getValue().eventType());
    }

    @Test
    void eliminar_inexistente_lanzaResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.eliminar(99L));

        verify(repository, never()).delete(any(Ciudadano.class));
    }
}
