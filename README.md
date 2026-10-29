# Plataforma-de-Reporte-de-Incidencias-Urbanas 

> **Alumnos:** *Lastra, Valentina y Medina, Lautaro*

# 📌 Descripción del Proyecto

Como proyecto final para la materia "Backend" este grupo propuso la idea de una **Plataforma de Reporte de Incidencias Urbanas** es un sistema distribuido basado en una arquitectura de **Microservicios** desarrollada con **Java 25** y **Spring Boot** ademas para cada entidad carasteristica de este sistema se usó **PostgreSQL** para almacenar datos de cada entidad. Su objetivo principal es gestionar de forma centralizada, trazable y automatizada los reportes de problemas en la vía pública (baches, luminarias, basura, etc.), vinculando a los ciudadanos, las zonas geográficas y las cuadrillas operativas mediante un motor de asignación inteligente.

El sistema implementa el patrón **Database per Service** (una base de datos independiente por cada microservicio) y utiliza **Apache Kafka** como broker de eventos para garantizar una comunicación asíncrona, escalable y desacoplada entre los distintos dominios de la aplicación.

---
# 📌 Sprints review
Hoy 29/10 presentamos este readme con el objetivo de avanzar en el codigo, para la siguiente sprint que será el 5/11 donde esperemos que el siguiente commit ya sea de codigo desarrollado de la aplicacion

#  Arquitectura del Sistema

A continuación se presenta el diagrama de la arquitectura, detallando el flujo desde el API Gateway hasta las bases de datos, los atributos principales de cada microservicio y la integración de eventos mediante Kafka:

```mermaid
flowchart LR
 subgraph Microservicios["ARQUITECTURA DE MICROSERVICIOS<br>s6"]
    direction TB
     M1["<b>1. MS Ciudadanos</b><br/>────────────<br/>id<br/>nombre<br/>email<br/>teléfono<br/>dni<br/>fecha_registro"]
    M2["<b> MS Incidencias</b><br/>────────────<br/>id<br/>título<br/>descripción<br/>tipo_incidencia<br/>foto<br/>prioridad<br/>estado_actual<br/>id_ciudadano<br/>id_zona<br/>id_cuadrilla<br/>fecha_creación<br/>fecha_cierre"]
    M3["<b>3. MS Asignaciones</b><br/>────────────<br/>id<br/>nombre<br/>especialidad<br/>id_zona<br/>estado<br/>reportes_activos"]
    M4["<b>4. MS Seguimiento</b><br/>────────────<br/>id<br/>id_reporte<br/>estado_anterior<br/>estado_nuevo<br/>fecha_cambio<br/>observaciones<br/>usuario_responsable<br/>foto_evidencia"]
    M5["<b>5. MS Zona</b><br/>────────────<br/>id<br/>nombre<br/>código_postal<br/>comuna<br/>límites_geográficos"]
  end
 subgraph Eventos["INTEGRACIÓN DE EVENTOS"]
    direction TB
        K["⚙️ KAFKA<br>Integracion de microservicios"]
  end
 subgraph BasesDatos["COMUNICACIÓN ENTRE BDs (PostgreSQL)"]
    direction TB
        D_USR[("<b>db_usuarios</b><br/>────────────<br/><b>Ciudadano</b><br/>id<br/>nombre<br/>email<br/>teléfono<br/>dni<br/>fecha_registro")]
        D_INC[("<b>db_incidencias</b><br/>────────────<br/><b>Reporte</b><br/>id<br/>título<br/>descripción<br/>tipo_incidencia<br/>foto<br/>prioridad<br/>estado_actual<br/>id_ciudadano<br/>id_zona<br/>id_cuadrilla<br/>fecha_creación<br/>fecha_cierre")]
        D_ASI[("<b>db_asignaciones</b><br/>────────────<br/><b>Cuadrilla</b><br/>id<br/>nombre<br/>especialidad<br/>id_zona<br/>estado<br/>reportes_activos")]
        D_SEG[("<b>db_seguimiento</b><br/>────────────<br/><b>Historial de Seguimiento</b><br/>id<br/>id_reporte<br/>estado_anterior<br/>estado_nuevo<br/>fecha_cambio<br/>observaciones<br/>usuario_responsable<br/>foto_evidencia")]
        D_ZONA[("<b>db_zonas</b><br/>────────────<br/><b>Zona</b><br/>id<br/>nombre<br/>código_postal<br/>comuna<br/>límites_geográficos")]
    end
    GW["<b>🛡️ API GATEWAY</b><br/>────────────<br/>Puerto: 8080<br/>Rol: Punto de Entrada Único<br/>────────────<br/>• Enrutamiento Dinámico<br/>• Seguridad (JWT)<br/>• Balanceo de Carga<br/>• Rate Limiting<br/>"] -- "sync-service" --> M1 & M2 & M3 & M4 & M5
    M1 -. "async-event" .-> K
    M2 -. "async-event" .-> K
    M3 -. "async-event" .-> K
    M4 -. "async-event" .-> K
    M5 -. "async-event" .-> K
    M1 --> D_USR
    M2 --> D_INC
    M3 --> D_ASI
    M4 --> D_SEG
    M5 --> D_ZONA

    classDef default fill:#fff,stroke:#333,stroke-width:2px;
