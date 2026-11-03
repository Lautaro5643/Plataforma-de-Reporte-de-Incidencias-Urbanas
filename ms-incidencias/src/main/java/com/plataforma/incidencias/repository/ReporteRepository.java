package com.plataforma.incidencias.repository;

import com.plataforma.incidencias.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Las búsquedas con filtros opcionales se resuelven con {@code Specification} desde el servicio. */
public interface ReporteRepository extends JpaRepository<Reporte, Long>, JpaSpecificationExecutor<Reporte> {
}
