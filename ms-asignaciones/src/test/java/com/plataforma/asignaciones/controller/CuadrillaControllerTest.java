package com.plataforma.asignaciones.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma.asignaciones.dto.CuadrillaRequestDTO;
import com.plataforma.asignaciones.dto.CuadrillaResponseDTO;
import com.plataforma.asignaciones.entity.Especialidad;
import com.plataforma.asignaciones.entity.EstadoCuadrilla;
import com.plataforma.asignaciones.service.CuadrillaService;
import com.plataforma.common.exception.BadRequestException;
import com.plataforma.common.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CuadrillaController.class)
class CuadrillaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CuadrillaService service;

    private CuadrillaResponseDTO response() {
        return new CuadrillaResponseDTO(1L, "Bacheo Norte", Especialidad.BACHEO, 10L, EstadoCuadrilla.DISPONIBLE, 0);
    }

    @Test
    void crear_datosValidos_devuelve201() throws Exception {
        CuadrillaRequestDTO request = new CuadrillaRequestDTO("Bacheo Norte", Especialidad.BACHEO, 10L, null);
        when(service.crear(any(CuadrillaRequestDTO.class))).thenReturn(response());

        mockMvc.perform(post("/api/cuadrillas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.especialidad").value("BACHEO"))
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"));
    }

    @Test
    void crear_sinNombreNiEspecialidad_devuelve400() throws Exception {
        CuadrillaRequestDTO invalido = new CuadrillaRequestDTO("", null, 10L, null);

        mockMvc.perform(post("/api/cuadrillas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void crear_especialidadInexistente_devuelve400() throws Exception {
        String json = "{\"nombre\":\"X\",\"especialidad\":\"VOLAR\",\"idZona\":10}";

        mockMvc.perform(post("/api/cuadrillas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listar_conFiltros_delegaEnElServicio() throws Exception {
        when(service.listar(10L, EstadoCuadrilla.DISPONIBLE)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/cuadrillas")
                        .param("idZona", "10")
                        .param("estado", "DISPONIBLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Bacheo Norte"));
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        when(service.obtenerPorId(99L)).thenThrow(new ResourceNotFoundException("Cuadrilla", 99L));

        mockMvc.perform(get("/api/cuadrillas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void asignacionSugerida_devuelveLaCuadrillaElegida() throws Exception {
        when(service.sugerirCuadrilla(10L, Especialidad.BACHEO)).thenReturn(response());

        mockMvc.perform(get("/api/cuadrillas/asignacion-sugerida")
                        .param("idZona", "10")
                        .param("especialidad", "BACHEO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void asignacionSugerida_sinParametros_devuelve400() throws Exception {
        mockMvc.perform(get("/api/cuadrillas/asignacion-sugerida"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void asignarReporte_devuelve200() throws Exception {
        when(service.asignarReporte(1L)).thenReturn(response());

        mockMvc.perform(post("/api/cuadrillas/1/asignar-reporte"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportesActivos").value(0));
    }

    @Test
    void liberarReporte_sinReportesActivos_devuelve400() throws Exception {
        when(service.liberarReporte(1L)).thenThrow(new BadRequestException("No tiene reportes activos"));

        mockMvc.perform(post("/api/cuadrillas/1/liberar-reporte"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No tiene reportes activos"));
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/cuadrillas/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1L);
    }
}
