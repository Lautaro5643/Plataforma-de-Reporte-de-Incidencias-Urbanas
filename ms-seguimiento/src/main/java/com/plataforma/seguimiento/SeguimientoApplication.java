package com.plataforma.seguimiento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.plataforma.seguimiento", "com.plataforma.common"})
public class SeguimientoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeguimientoApplication.class, args);
    }
}
