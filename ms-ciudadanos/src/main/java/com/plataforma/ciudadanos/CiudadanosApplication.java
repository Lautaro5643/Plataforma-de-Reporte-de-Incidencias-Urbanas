package com.plataforma.ciudadanos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.plataforma.ciudadanos", "com.plataforma.common"})
public class CiudadanosApplication {

    public static void main(String[] args) {
        SpringApplication.run(CiudadanosApplication.class, args);
    }
}
