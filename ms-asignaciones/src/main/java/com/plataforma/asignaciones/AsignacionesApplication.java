package com.plataforma.asignaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.plataforma.asignaciones", "com.plataforma.common"})
public class AsignacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsignacionesApplication.class, args);
    }
}
