package com.plataforma.seguimiento.repository;

import com.plataforma.seguimiento.entity.HistorialSeguimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialRepository extends JpaRepository<HistorialSeguimiento, Long> {

    List<HistorialSeguimiento> findAllByOrderByFechaCambioDesc();

    List<HistorialSeguimiento> findByIdReporteOrderByFechaCambioDesc(Long idReporte);
}
