package com.plataforma.zonas.repository;

import com.plataforma.zonas.entity.Zona;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZonaRepository extends JpaRepository<Zona, Long> {

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    List<Zona> findByCodigoPostal(String codigoPostal);
}
