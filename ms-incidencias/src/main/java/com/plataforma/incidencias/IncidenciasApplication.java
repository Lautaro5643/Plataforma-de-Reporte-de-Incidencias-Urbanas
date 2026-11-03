package com.plataforma.incidencias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.plataforma.incidencias", "com.plataforma.common"})
public class IncidenciasApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidenciasApplication.class, args);
    }
}
