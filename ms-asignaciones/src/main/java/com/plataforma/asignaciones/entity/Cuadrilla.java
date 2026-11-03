package com.plataforma.asignaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cuadrillas")
public class Cuadrilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Especialidad especialidad;

    /** Referencia lógica a la zona (vive en ms-zonas, no hay FK entre bases). */
    @Column(name = "id_zona", nullable = false)
    private Long idZona;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCuadrilla estado;

    @Column(name = "reportes_activos", nullable = false)
    private int reportesActivos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }

    public Long getIdZona() { return idZona; }
    public void setIdZona(Long idZona) { this.idZona = idZona; }

    public EstadoCuadrilla getEstado() { return estado; }
    public void setEstado(EstadoCuadrilla estado) { this.estado = estado; }

    public int getReportesActivos() { return reportesActivos; }
    public void setReportesActivos(int reportesActivos) { this.reportesActivos = reportesActivos; }
}
