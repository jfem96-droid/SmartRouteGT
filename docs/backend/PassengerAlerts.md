# Módulo de Alertas de Pasajeros

## 1. Objetivo

El módulo de alertas de pasajeros permite registrar y consultar alertas generadas por los usuarios del sistema SmartRoute GT.

Las alertas pueden utilizarse para reportar emergencias, problemas con conductores, inconvenientes con rutas u otras situaciones.

## 2. Tipos de alerta

Actualmente se manejan los siguientes tipos:

- SOS
- DRIVER_BEHAVIOR
- ROUTE_PROBLEM
- OTHER

## 3. Estados

Toda alerta nueva inicia automáticamente con estado:

PENDING

Posteriormente podrá pasar a:

- REVIEWED
- RESOLVED

## 4. Datos almacenados

Cada alerta contiene:

- id
- tipo
- descripcion
- latitud
- longitud
- fechaHora
- estado

La fecha y hora se generan automáticamente al crear la alerta.

## 5. Endpoints

### Crear alerta

```http
POST /api/passenger-alerts