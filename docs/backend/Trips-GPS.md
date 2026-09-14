# Módulo Trips y GPS

## 1. Objetivo

El módulo Trips y GPS permite registrar los viajes activos de los buses y almacenar las ubicaciones GPS asociadas a cada viaje.

El módulo permite:

- Iniciar un viaje.
- Finalizar un viaje.
- Registrar ubicaciones GPS durante un viaje.
- Consultar las ubicaciones registradas de un viaje.
- Consultar la última ubicación conocida de un bus.

---

## 2. Funcionamiento

El flujo principal es:

1. Se inicia un viaje utilizando una asignación existente.
2. El sistema verifica que el viaje pueda iniciarse.
3. Mientras el viaje está activo se pueden registrar ubicaciones GPS.
4. Las ubicaciones se almacenan asociadas al viaje.
5. Al finalizar el recorrido, el viaje se marca como inactivo.
6. Las ubicaciones pueden consultarse posteriormente.

---

## 3. Entidades utilizadas

### Trip

Representa un viaje realizado por un bus mediante una asignación de conductor, bus y ruta.

Entre sus datos se encuentran:

- Identificador del viaje.
- Asignación utilizada.
- Fecha y hora de inicio.
- Fecha y hora de finalización.
- Estado activo/inactivo.

### BusLocation

Representa una ubicación GPS registrada durante un viaje.

Incluye información como:

- Viaje asociado.
- Latitud.
- Longitud.
- Fecha y hora de registro.

---

## 4. Endpoints implementados

### Iniciar un viaje

```http
POST /api/trips/start?assignmentId={id}

