package com.plataforma.ciudadanos.repository;

import com.plataforma.ciudadanos.entity.Ciudadano;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CiudadanoRepository extends JpaRepository<Ciudadano, Long> {

    boolean existsByEmail(String email);

    boolean existsByDni(String dni);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByDniAndIdNot(String dni, Long id);

    Optional<Ciudadano> findByDni(String dni);
}
