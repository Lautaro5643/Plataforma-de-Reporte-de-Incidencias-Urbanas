package com.plataforma.zonas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.plataforma.zonas", "com.plataforma.common"})
public class ZonasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZonasApplication.class, args);
    }
}
