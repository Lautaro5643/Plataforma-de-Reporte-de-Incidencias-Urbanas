package com.plataforma.zonas.controller;

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
import com.plataforma.common.exception.ConflictException;
import com.plataforma.common.exception.ResourceNotFoundException;
import com.plataforma.zonas.dto.ZonaRequestDTO;
import com.plataforma.zonas.dto.ZonaResponseDTO;
import com.plataforma.zonas.service.ZonaService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ZonaController.class)
class ZonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ZonaService service;

    private ZonaRequestDTO request() {
        return new ZonaRequestDTO("Centro", "4000", "Comuna 1", null);
    }

    private ZonaResponseDTO response() {
        return new ZonaResponseDTO(1L, "Centro", "4000", "Comuna 1", null);
    }

    @Test
    void crear_datosValidos_devuelve201() throws Exception {
        when(service.crear(any(ZonaRequestDTO.class))).thenReturn(response());

        mockMvc.perform(post("/api/zonas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Centro"));
    }

    @Test
    void crear_sinNombre_devuelve400() throws Exception {
        ZonaRequestDTO invalido = new ZonaRequestDTO("", "4000", "Comuna 1", null);

        mockMvc.perform(post("/api/zonas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void crear_nombreDuplicado_devuelve409() throws Exception {
        when(service.crear(any(ZonaRequestDTO.class)))
                .thenThrow(new ConflictException("Ya existe una zona con el nombre Centro"));

        mockMvc.perform(post("/api/zonas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isConflict());
    }

    @Test
    void listar_conCodigoPostal_delegaEnElServicio() throws Exception {
        when(service.listar("4000")).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/zonas").param("codigoPostal", "4000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoPostal").value("4000"));
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        when(service.obtenerPorId(99L)).thenThrow(new ResourceNotFoundException("Zona", 99L));

        mockMvc.perform(get("/api/zonas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Zona no encontrado con id: 99"));
    }

    @Test
    void actualizar_datosValidos_devuelve200() throws Exception {
        when(service.actualizar(any(Long.class), any(ZonaRequestDTO.class))).thenReturn(response());

        mockMvc.perform(put("/api/zonas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comuna").value("Comuna 1"));
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/zonas/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1L);
    }
}
