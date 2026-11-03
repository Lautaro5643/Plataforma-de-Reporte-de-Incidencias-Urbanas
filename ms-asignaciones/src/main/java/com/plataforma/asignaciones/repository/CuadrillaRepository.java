package com.plataforma.asignaciones.repository;

import com.plataforma.asignaciones.entity.Cuadrilla;
import com.plataforma.asignaciones.entity.EstadoCuadrilla;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuadrillaRepository extends JpaRepository<Cuadrilla, Long> {

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    List<Cuadrilla> findByIdZona(Long idZona);

    List<Cuadrilla> findByEstado(EstadoCuadrilla estado);

    List<Cuadrilla> findByIdZonaAndEstado(Long idZona, EstadoCuadrilla estado);
}
