# Especificación de Endpoints y Servicios

## Microservicios del Sistema
1. **MS Ciudadanos** (`/api/v1/ciudadanos`)
2. **MS Incidencias** (`/api/v1/incidencias`)
3. **MS Asignaciones** (`/api/v1/asignaciones`)
4. **MS Seguimiento** (`/api/v1/seguimiento`)
5. **MS Zona** (`/api/v1/zonas`)

## Casos de Prueba Previstos (JUnit 5)
- Validación de datos requeridos al crear un reporte.
- Comprobación de asignación de cuadrilla por especialidad y zona.
- Verificación de registro de auditoría en cambios de estado.