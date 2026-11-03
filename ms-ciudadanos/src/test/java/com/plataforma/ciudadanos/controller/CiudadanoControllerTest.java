package com.plataforma.ciudadanos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma.ciudadanos.dto.CiudadanoRequestDTO;
import com.plataforma.ciudadanos.dto.CiudadanoResponseDTO;
import com.plataforma.ciudadanos.service.CiudadanoService;
import com.plataforma.common.exception.ConflictException;
import com.plataforma.common.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CiudadanoController.class)
class CiudadanoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CiudadanoService service;

    private CiudadanoRequestDTO request() {
        return new CiudadanoRequestDTO("Juan Pérez", "juan@mail.com", "3814001122", "30123456");
    }

    private CiudadanoResponseDTO response() {
        return new CiudadanoResponseDTO(1L, "Juan Pérez", "juan@mail.com", "3814001122", "30123456",
                LocalDateTime.of(2026, 10, 1, 10, 0));
    }

    @Test
    void crear_datosValidos_devuelve201() throws Exception {
        when(service.crear(any(CiudadanoRequestDTO.class))).thenReturn(response());

        mockMvc.perform(post("/api/ciudadanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("juan@mail.com"));
    }

    @Test
    void crear_emailInvalido_devuelve400ConDetalles() throws Exception {
        CiudadanoRequestDTO invalido = new CiudadanoRequestDTO("Juan", "no-es-un-email", null, "30123456");

        mockMvc.perform(post("/api/ciudadanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void crear_dniConLetras_devuelve400() throws Exception {
        CiudadanoRequestDTO invalido = new CiudadanoRequestDTO("Juan", "juan@mail.com", null, "ABC123");

        mockMvc.perform(post("/api/ciudadanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_emailDuplicado_devuelve409() throws Exception {
        when(service.crear(any(CiudadanoRequestDTO.class)))
                .thenThrow(new ConflictException("Ya existe un ciudadano con el email juan@mail.com"));

        mockMvc.perform(post("/api/ciudadanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe un ciudadano con el email juan@mail.com"));
    }

    @Test
    void listar_devuelveLista() throws Exception {
        when(service.listar()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/ciudadanos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dni").value("30123456"));
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        when(service.obtenerPorId(99L)).thenThrow(new ResourceNotFoundException("Ciudadano", 99L));

        mockMvc.perform(get("/api/ciudadanos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ciudadano no encontrado con id: 99"));
    }

    @Test
    void obtenerPorDni_existente_devuelve200() throws Exception {
        when(service.obtenerPorDni("30123456")).thenReturn(response());

        mockMvc.perform(get("/api/ciudadanos/dni/30123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"));
    }

    @Test
    void actualizar_datosValidos_devuelve200() throws Exception {
        when(service.actualizar(any(Long.class), any(CiudadanoRequestDTO.class))).thenReturn(response());

        mockMvc.perform(put("/api/ciudadanos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/ciudadanos/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1L);
    }
}
