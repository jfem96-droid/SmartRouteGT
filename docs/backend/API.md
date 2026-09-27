# API del Backend - SmartRoute GT

## 1. Objetivo

Este documento describe los endpoints disponibles actualmente en el backend de SmartRoute GT.

La documentación se basa en los controladores y entidades existentes en el proyecto.

URL base local: http://localhost:8080

---

## 2. Estado del backend

### Consultar estado

GET /api/status

---

## 3. Algoritmos de grafos

### 3.1 Agregar conexión

POST /api/algorithms/connection

Parámetros:
- origen: ID del nodo de origen.
- destino: ID del nodo destino.
- peso: peso de la conexión.

Ejemplo:
POST http://localhost:8080/api/algorithms/connection?origen=1&destino=2&peso=5

### 3.2 Dijkstra

GET /api/algorithms/dijkstra

Parámetros:
- origen: nodo de origen.
- destino: nodo destino.

Ejemplo:
GET http://localhost:8080/api/algorithms/dijkstra?origen=1&destino=5

El resultado corresponde a una lista de identificadores que representa el camino calculado.

### 3.3 Dijkstra aplicado a una ruta

GET /api/algorithms/route/dijkstra

Parámetros:
- routeId: ID de la ruta.
- origen: nodo de origen.
- destino: nodo destino.

Ejemplo:
GET http://localhost:8080/api/algorithms/route/dijkstra?routeId=1&origen=1&destino=5

---

## 4. Recorridos BFS y DFS

Base: /api/bfs-dfs

### 4.1 BFS

GET /api/bfs-dfs/bfs

Parámetro:
- startStopId: ID de la parada inicial.

Ejemplo:
GET http://localhost:8080/api/bfs-dfs/bfs?startStopId=1

### 4.2 DFS

GET /api/bfs-dfs/dfs

Parámetro:
- startStopId: ID de la parada inicial.

Ejemplo:
GET http://localhost:8080/api/bfs-dfs/dfs?startStopId=1

---

## 5. Búsqueda de buses

Base: /api/search

### 5.1 Búsqueda lineal

GET /api/search/buses/linear/{codigo}

Ejemplo:
GET http://localhost:8080/api/search/buses/linear/BUS-001

### 5.2 Búsqueda binaria

GET /api/search/buses/binary/{codigo}

Ejemplo:
GET http://localhost:8080/api/search/buses/binary/BUS-001

Si el bus no existe, el backend responde con 404 Not Found.

---

## 6. Ordenamiento de buses

### Merge Sort por capacidad

GET /api/merge-sort/buses/capacidad

Ejemplo:
GET http://localhost:8080/api/merge-sort/buses/capacidad

Este endpoint devuelve los buses ordenados por capacidad mediante Merge Sort.

---

## 7. Buses

Base: /api/buses

### 7.1 Listar buses

GET /api/buses/

### 7.2 Crear bus

POST /api/buses/

Cuerpo JSON:

{
  "codigo": "BUS-001",
  "placa": "P123ABC",
  "capacidad": 40,
  "activa": true
}

### 7.3 Consultar bus

GET /api/buses/{id}

### 7.4 Actualizar bus

PUT /api/buses/{id}

### 7.5 Desactivar bus

DELETE /api/buses/{id}

La operación corresponde a una desactivación lógica.

### 7.6 Activar bus

PUT /api/buses/{id}/activar

---

## 8. Pilotos

Base: /api/drivers

### 8.1 Listar pilotos

GET /api/drivers/

### 8.2 Listar pilotos ordenados por sanciones

GET /api/drivers/ordenados-por-sanciones

### 8.3 Crear piloto

POST /api/drivers/

Cuerpo JSON:

{
  "nombre": "Nombre del piloto",
  "licencia": "LIC-001",
  "telefono": "55555555",
  "sancionesConfirmadas": 0,
  "activo": true
}

### 8.4 Buscar piloto por licencia

GET /api/drivers/license/{licencia}

### 8.5 Consultar piloto

GET /api/drivers/{id}

### 8.6 Actualizar piloto

PUT /api/drivers/{id}

### 8.7 Desactivar piloto

DELETE /api/drivers/{id}

La operación corresponde a una desactivación lógica.

### 8.8 Activar piloto

PUT /api/drivers/{id}/activar

---

## 9. Rutas

Base: /api/routes

### 9.1 Listar rutas

GET /api/routes/

### 9.2 Crear ruta

POST /api/routes/

Cuerpo JSON:

{
  "codigo": "R-001",
  "nombre": "Ruta Principal",
  "activa": true
}

### 9.3 Consultar ruta

GET /api/routes/{id}

### 9.4 Actualizar ruta

PUT /api/routes/{id}

### 9.5 Desactivar ruta

DELETE /api/routes/{id}

La operación corresponde a una desactivación lógica.

### 9.6 Activar ruta

PUT /api/routes/{id}/activar

---

## 10. Paradas

Base: /api/stops

### 10.1 Listar paradas

GET /api/stops/

### 10.2 Crear parada

POST /api/stops/

Cuerpo JSON:

{
  "nombre": "Parada Central",
  "latitud": 14.6349,
  "longitud": -90.5069,
  "radioPermitido": 50,
  "activa": true
}

Validaciones principales:
- El nombre es obligatorio.
- La latitud debe estar entre -90 y 90.
- La longitud debe estar entre -180 y 180.
- El radio permitido debe ser mayor que 0.

### 10.3 Consultar parada

GET /api/stops/{id}

### 10.4 Actualizar parada

PUT /api/stops/{id}

La actualización permite modificar:
- Nombre.
- Latitud.
- Longitud.
- Radio permitido.

### 10.5 Desactivar parada

DELETE /api/stops/{id}

### 10.6 Activar parada

PUT /api/stops/{id}/activar

---

## 11. Asignaciones de piloto, bus y ruta

Base: /api/assignments

### 11.1 Listar asignaciones

GET /api/assignments/

### 11.2 Buscar asignaciones por piloto

GET /api/assignments/driver/{driverId}

### 11.3 Crear asignación

POST /api/assignments/

Cuerpo JSON:

{
  "driverId": 1,
  "busId": 1,
  "routeId": 1
}

El backend valida que:
- El piloto exista y esté activo.
- El bus exista y esté activo.
- La ruta exista y esté activa.

---

## 12. Paradas de una ruta

Base: /api/route-stops

### 12.1 Listar relaciones ruta-parada

GET /api/route-stops/

### 12.2 Agregar parada a una ruta

POST /api/route-stops/

Parámetros:
- routeId: ID de la ruta.
- stopId: ID de la parada.
- orden: posición de la parada dentro de la ruta.

Ejemplo:
POST http://localhost:8080/api/route-stops/?routeId=1&stopId=2&orden=1

### 12.3 Consultar paradas de una ruta

GET /api/route-stops/route

Parámetro:
- routeId: ID de la ruta.

Ejemplo:
GET http://localhost:8080/api/route-stops/route?routeId=1

Las paradas se devuelven ordenadas según el campo orden.

---

## 13. Viajes

Base: /api/trips

### 13.1 Iniciar viaje

POST /api/trips/start

Parámetro:
- assignmentId: ID de la asignación.

Ejemplo:
POST http://localhost:8080/api/trips/start?assignmentId=1

Antes de iniciar el viaje se valida que:
- La asignación exista.
- La asignación esté activa.
- El piloto esté activo.
- El bus esté activo.
- La ruta esté activa.
- El piloto no tenga otro viaje activo.

### 13.2 Finalizar viaje

PUT /api/trips/{id}/finish

Ejemplo:
PUT http://localhost:8080/api/trips/1/finish

### 13.3 Consultar viaje activo de un piloto

GET /api/trips/driver/{driverId}/active

### 13.4 Consultar siguiente parada

GET /api/trips/{tripId}/next-stop

El endpoint obtiene la siguiente parada pendiente del viaje y puede incluir la distancia calculada desde la ubicación más reciente del bus.

---

## 14. Ubicaciones de buses

Base: /api/locations

### 14.1 Registrar ubicación

POST /api/locations/

Parámetros:
- tripId: ID del viaje.
- latitud: latitud actual.
- longitud: longitud actual.
- velocidad: velocidad registrada.

Ejemplo:
POST http://localhost:8080/api/locations/?tripId=1&latitud=14.6349&longitud=-90.5069&velocidad=45

Validaciones:
- El viaje debe existir.
- El viaje debe estar activo.
- Latitud entre -90 y 90.
- Longitud entre -180 y 180.
- La velocidad no puede ser negativa.

El backend utiliza un límite de velocidad de 60.0 y controla el exceso sostenido para generar una infracción cuando corresponde.

### 14.2 Consultar ubicaciones de un viaje

GET /api/locations/trip/{tripId}

Las ubicaciones se devuelven ordenadas cronológicamente.

### 14.3 Consultar última ubicación de un bus

GET /api/locations/bus/{busId}/latest

---

## 15. Monitoreo de buses

Base: /api/monitoring

### Consultar buses para monitoreo

GET /api/monitoring/buses

El endpoint proporciona información relacionada con:
- Bus.
- Piloto asignado.
- Ruta asignada.
- Viaje activo.
- Última ubicación.
- Velocidad.
- Fecha de última actualización.

Los datos de asignación, viaje y ubicación pueden ser nulos cuando no existe información activa para el bus.

---

## 16. Bus más cercano

GET /api/algorithms/nearest-bus

Parámetros:
- latitud: latitud de referencia.
- longitud: longitud de referencia.

Ejemplo:
GET http://localhost:8080/api/algorithms/nearest-bus?latitud=14.6349&longitud=-90.5069

---

## 17. Alertas de pasajeros

Base: /api/passenger-alerts

### 17.1 Crear alerta

POST /api/passenger-alerts/

Cuerpo JSON:

{
  "tipo": "<tipo>",
  "descripcion": "Descripción de la alerta",
  "latitud": 14.6349,
  "longitud": -90.5069,
  "fechaHora": "2026-09-26T20:00:00",
  "estado": "<estado>"
}

Los valores de tipo y estado deben corresponder a los enums definidos en el backend.

### 17.2 Listar alertas

GET /api/passenger-alerts/

### 17.3 Consultar alerta

GET /api/passenger-alerts/{id}

Si la alerta no existe, el backend responde con 404 Not Found.

---

## 18. Infracciones

Base: /api/violations

### 18.1 Crear infracción

POST /api/violations/

Cuerpo JSON:

{
  "driverId": 1,
  "busId": 1,
  "tripId": 1,
  "tipo": "SPEEDING",
  "descripcion": "Exceso de velocidad durante más de 10 segundos",
  "velocidadRegistrada": 75.0,
  "limitePermitido": 60.0
}

El backend valida la existencia del piloto, bus y viaje antes de crear la infracción.

### 18.2 Listar infracciones

GET /api/violations/

### 18.3 Consultar infracción

GET /api/violations/{id}

---

## 19. Resumen de endpoints

| Módulo | Método | Endpoint |
|---|---|---|
| Estado | GET | /api/status |
| Algoritmos | POST | /api/algorithms/connection |
| Dijkstra | GET | /api/algorithms/dijkstra |
| Dijkstra por ruta | GET | /api/algorithms/route/dijkstra |
| BFS | GET | /api/bfs-dfs/bfs |
| DFS | GET | /api/bfs-dfs/dfs |
| Búsqueda lineal | GET | /api/search/buses/linear/{codigo} |
| Búsqueda binaria | GET | /api/search/buses/binary/{codigo} |
| Merge Sort | GET | /api/merge-sort/buses/capacidad |
| Buses | GET/POST | /api/buses/ |
| Bus individual | GET/PUT/DELETE | /api/buses/{id} |
| Activar bus | PUT | /api/buses/{id}/activar |
| Pilotos | GET/POST | /api/drivers/ |
| Piloto individual | GET/PUT/DELETE | /api/drivers/{id} |
| Activar piloto | PUT | /api/drivers/{id}/activar |
| Buscar licencia | GET | /api/drivers/license/{licencia} |
| Rutas | GET/POST | /api/routes/ |
| Ruta individual | GET/PUT/DELETE | /api/routes/{id} |
| Activar ruta | PUT | /api/routes/{id}/activar |
| Paradas | GET/POST | /api/stops/ |
| Parada individual | GET/PUT/DELETE | /api/stops/{id} |
| Activar parada | PUT | /api/stops/{id}/activar |
| Asignaciones | GET/POST | /api/assignments/ |
| Asignaciones por piloto | GET | /api/assignments/driver/{driverId} |
| Ruta-parada | GET/POST | /api/route-stops/ |
| Paradas por ruta | GET | /api/route-stops/route |
| Iniciar viaje | POST | /api/trips/start |
| Finalizar viaje | PUT | /api/trips/{id}/finish |
| Viaje activo | GET | /api/trips/driver/{driverId}/active |
| Siguiente parada | GET | /api/trips/{tripId}/next-stop |
| Registrar ubicación | POST | /api/locations/ |
| Ubicaciones por viaje | GET | /api/locations/trip/{tripId} |
| Última ubicación | GET | /api/locations/bus/{busId}/latest |
| Monitoreo | GET | /api/monitoring/buses |
| Bus más cercano | GET | /api/algorithms/nearest-bus |
| Alertas | GET/POST | /api/passenger-alerts/ |
| Alerta individual | GET | /api/passenger-alerts/{id} |
| Infracciones | GET/POST | /api/violations/ |
| Infracción individual | GET | /api/violations/{id} |

---

## 20. Notas

- Todos los endpoints se encuentran bajo la URL base local http://localhost:8080.
- Los ejemplos utilizan identificadores ficticios como 1.
- Las operaciones DELETE de buses, pilotos, rutas y paradas corresponden a desactivaciones lógicas según la implementación actual.
- Las respuestas dependen de los datos almacenados en la base de datos.
- Los valores de los enums deben coincidir con las definiciones existentes en el código fuente.
- Esta documentación describe los endpoints identificados en los controladores actuales del backend de SmartRoute GT.