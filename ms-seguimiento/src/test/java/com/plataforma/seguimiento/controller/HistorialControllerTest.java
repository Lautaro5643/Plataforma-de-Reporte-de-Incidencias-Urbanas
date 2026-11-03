package com.plataforma.seguimiento.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma.common.exception.ResourceNotFoundException;
import com.plataforma.seguimiento.dto.HistorialRequestDTO;
import com.plataforma.seguimiento.dto.HistorialResponseDTO;
import com.plataforma.seguimiento.service.HistorialService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HistorialController.class)
class HistorialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HistorialService service;

    private HistorialResponseDTO response() {
        return new HistorialResponseDTO(1L, 10L, "CREADO", "ASIGNADO", LocalDateTime.of(2026, 10, 31, 12, 0),
                "Se asignó cuadrilla", "operador1", null);
    }

    @Test
    void registrar_datosValidos_devuelve201() throws Exception {
        HistorialRequestDTO request = new HistorialRequestDTO(10L, "CREADO", "ASIGNADO", "Se asignó cuadrilla",
                "operador1", null);
        when(service.registrar(any(HistorialRequestDTO.class))).thenReturn(response());

        mockMvc.perform(post("/api/seguimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estadoNuevo").value("ASIGNADO"));
    }

    @Test
    void registrar_sinReporteNiEstadoNuevo_devuelve400() throws Exception {
        HistorialRequestDTO invalido = new HistorialRequestDTO(null, null, "", null, null, null);

        mockMvc.perform(post("/api/seguimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void listar_devuelveLasEntradas() throws Exception {
        when(service.listar()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/seguimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idReporte").value(10));
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        when(service.obtenerPorId(99L))
                .thenThrow(new ResourceNotFoundException("Historial de seguimiento", 99L));

        mockMvc.perform(get("/api/seguimientos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorReporte_devuelveElHistorialDelReporte() throws Exception {
        when(service.listarPorReporte(10L)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/seguimientos/reporte/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estadoAnterior").value("CREADO"));
    }
}
