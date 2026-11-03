package com.plataforma.incidencias.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma.common.exception.BadRequestException;
import com.plataforma.common.exception.ResourceNotFoundException;
import com.plataforma.incidencias.dto.AsignarCuadrillaRequestDTO;
import com.plataforma.incidencias.dto.CambioEstadoRequestDTO;
import com.plataforma.incidencias.dto.ReporteRequestDTO;
import com.plataforma.incidencias.dto.ReporteResponseDTO;
import com.plataforma.incidencias.entity.EstadoReporte;
import com.plataforma.incidencias.entity.Prioridad;
import com.plataforma.incidencias.entity.TipoIncidencia;
import com.plataforma.incidencias.service.ReporteService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReporteController.class)
class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReporteService service;

    private ReporteRequestDTO request() {
        return new ReporteRequestDTO("Bache en Av. Mitre", "Bache profundo", TipoIncidencia.BACHE, null, null, 1L, 2L);
    }

    private ReporteResponseDTO response(EstadoReporte estado) {
        return new ReporteResponseDTO(1L, "Bache en Av. Mitre", "Bache profundo", TipoIncidencia.BACHE, null,
                Prioridad.MEDIA, estado, 1L, 2L, null, LocalDateTime.of(2026, 10, 30, 9, 0), null);
    }

    @Test
    void crear_datosValidos_devuelve201() throws Exception {
        when(service.crear(any(ReporteRequestDTO.class))).thenReturn(response(EstadoReporte.CREADO));

        mockMvc.perform(post("/api/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estadoActual").value("CREADO"))
                .andExpect(jsonPath("$.tipoIncidencia").value("BACHE"));
    }

    @Test
    void crear_sinCiudadanoNiZona_devuelve400() throws Exception {
        ReporteRequestDTO invalido = new ReporteRequestDTO("Bache", null, TipoIncidencia.BACHE, null, null, null, null);

        mockMvc.perform(post("/api/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void crear_tipoDeIncidenciaInexistente_devuelve400() throws Exception {
        String json = "{\"titulo\":\"X\",\"tipoIncidencia\":\"TERREMOTO\",\"idCiudadano\":1,\"idZona\":2}";

        mockMvc.perform(post("/api/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listar_conFiltroDeEstado_delegaEnElServicio() throws Exception {
        when(service.listar(EstadoReporte.CREADO, null, null)).thenReturn(List.of(response(EstadoReporte.CREADO)));

        mockMvc.perform(get("/api/reportes").param("estado", "CREADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void listar_conEstadoInvalido_devuelve400() throws Exception {
        mockMvc.perform(get("/api/reportes").param("estado", "INEXISTENTE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        when(service.obtenerPorId(99L)).thenThrow(new ResourceNotFoundException("Reporte", 99L));

        mockMvc.perform(get("/api/reportes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Reporte no encontrado con id: 99"));
    }

    @Test
    void actualizar_datosValidos_devuelve200() throws Exception {
        when(service.actualizar(eq(1L), any(ReporteRequestDTO.class))).thenReturn(response(EstadoReporte.CREADO));

        mockMvc.perform(put("/api/reportes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Bache en Av. Mitre"));
    }

    @Test
    void cambiarEstado_transicionValida_devuelve200() throws Exception {
        when(service.cambiarEstado(eq(1L), any(CambioEstadoRequestDTO.class)))
                .thenReturn(response(EstadoReporte.EN_PROCESO));

        mockMvc.perform(patch("/api/reportes/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CambioEstadoRequestDTO(EstadoReporte.EN_PROCESO, "En marcha", "operador1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoActual").value("EN_PROCESO"));
    }

    @Test
    void cambiarEstado_transicionInvalida_devuelve400() throws Exception {
        when(service.cambiarEstado(eq(1L), any(CambioEstadoRequestDTO.class)))
                .thenThrow(new BadRequestException("Transición de estado inválida: CREADO -> CERRADO"));

        mockMvc.perform(patch("/api/reportes/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CambioEstadoRequestDTO(EstadoReporte.CERRADO, null, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Transición de estado inválida: CREADO -> CERRADO"));
    }

    @Test
    void cambiarEstado_sinNuevoEstado_devuelve400() throws Exception {
        mockMvc.perform(patch("/api/reportes/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void asignarCuadrilla_datosValidos_devuelve200() throws Exception {
        ReporteResponseDTO asignado = new ReporteResponseDTO(1L, "Bache en Av. Mitre", "Bache profundo",
                TipoIncidencia.BACHE, null, Prioridad.MEDIA, EstadoReporte.ASIGNADO, 1L, 2L, 7L,
                LocalDateTime.of(2026, 10, 30, 9, 0), null);
        when(service.asignarCuadrilla(eq(1L), any(AsignarCuadrillaRequestDTO.class))).thenReturn(asignado);

        mockMvc.perform(put("/api/reportes/1/cuadrilla")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AsignarCuadrillaRequestDTO(7L, "operador1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCuadrilla").value(7))
                .andExpect(jsonPath("$.estadoActual").value("ASIGNADO"));
    }

    @Test
    void asignarCuadrilla_sinCuadrilla_devuelve400() throws Exception {
        mockMvc.perform(put("/api/reportes/1/cuadrilla")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/reportes/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1L);
    }
}
