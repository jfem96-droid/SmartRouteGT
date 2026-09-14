# Módulo de Infracciones

## 1. Objetivo

El módulo de infracciones permite registrar y consultar infracciones detectadas durante los viajes de SmartRoute GT.

Una infracción queda relacionada con:

- piloto
- bus
- viaje

## 2. Datos registrados

Cada infracción contiene:

- id
- driver
- bus
- trip
- tipo
- descripcion
- velocidadRegistrada
- limitePermitido
- fechaHora
- estado

## 3. Estados

Actualmente una infracción puede tener los estados:

- PENDING
- CONFIRMED
- REJECTED

Toda infracción nueva inicia automáticamente como:

PENDING

Esto significa que todavía debe ser revisada antes de convertirse en una sanción confirmada.

## 4. Endpoints

### Crear infracción

```http
POST /api/violations