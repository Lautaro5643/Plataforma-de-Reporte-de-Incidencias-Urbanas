package com.plataforma.incidencias.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_incidencia", nullable = false, length = 30)
    private TipoIncidencia tipoIncidencia;

    /** URL o ruta de la foto adjunta por el ciudadano. */
    @Column(length = 500)
    private String foto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridad prioridad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_actual", nullable = false, length = 20)
    private EstadoReporte estadoActual;

    /** Referencia lógica al ciudadano (vive en ms-ciudadanos). */
    @Column(name = "id_ciudadano", nullable = false)
    private Long idCiudadano;

    /** Referencia lógica a la zona (vive en ms-zonas). */
    @Column(name = "id_zona", nullable = false)
    private Long idZona;

    /** Referencia lógica a la cuadrilla asignada (vive en ms-asignaciones); nula hasta que se asigna. */
    @Column(name = "id_cuadrilla")
    private Long idCuadrilla;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public TipoIncidencia getTipoIncidencia() { return tipoIncidencia; }
    public void setTipoIncidencia(TipoIncidencia tipoIncidencia) { this.tipoIncidencia = tipoIncidencia; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }

    public EstadoReporte getEstadoActual() { return estadoActual; }
    public void setEstadoActual(EstadoReporte estadoActual) { this.estadoActual = estadoActual; }

    public Long getIdCiudadano() { return idCiudadano; }
    public void setIdCiudadano(Long idCiudadano) { this.idCiudadano = idCiudadano; }

    public Long getIdZona() { return idZona; }
    public void setIdZona(Long idZona) { this.idZona = idZona; }

    public Long getIdCuadrilla() { return idCuadrilla; }
    public void setIdCuadrilla(Long idCuadrilla) { this.idCuadrilla = idCuadrilla; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
}
