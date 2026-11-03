package com.plataforma.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Enrutamiento del API Gateway hacia cada microservicio. */
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder,
                               @Value("${services.ciudadanos.url}") String ciudadanosUrl,
                               @Value("${services.incidencias.url}") String incidenciasUrl,
                               @Value("${services.asignaciones.url}") String asignacionesUrl,
                               @Value("${services.seguimiento.url}") String seguimientoUrl,
                               @Value("${services.zonas.url}") String zonasUrl) {
        return builder.routes()
                .route("ms-ciudadanos", r -> r.path("/api/ciudadanos/**").uri(ciudadanosUrl))
                .route("ms-incidencias", r -> r.path("/api/reportes/**").uri(incidenciasUrl))
                .route("ms-asignaciones", r -> r.path("/api/cuadrillas/**").uri(asignacionesUrl))
                .route("ms-seguimiento", r -> r.path("/api/seguimientos/**").uri(seguimientoUrl))
                .route("ms-zonas", r -> r.path("/api/zonas/**").uri(zonasUrl))
                .build();
    }
}
