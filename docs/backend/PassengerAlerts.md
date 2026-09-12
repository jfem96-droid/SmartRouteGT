# Módulo de Alertas de Pasajeros

## 1. Objetivo

El módulo de alertas de pasajeros está orientado a proporcionar información útil para los usuarios del sistema de transporte.

Actualmente el backend cuenta con funcionalidades relacionadas con la ubicación de buses y la búsqueda del bus más cercano, las cuales sirven como base para futuras alertas dirigidas a pasajeros.

---

## 2. Funcionamiento

El sistema puede consultar la ubicación más reciente de los buses y determinar cuál se encuentra más cerca de una posición determinada.

Esta información puede utilizarse posteriormente para implementar funcionalidades como:

- Mostrar buses cercanos.
- Informar al pasajero sobre la ubicación de un bus.
- Generar alertas relacionadas con la proximidad de un bus.

---

## 3. Funcionalidad relacionada implementada

Actualmente existe la búsqueda del bus más cercano mediante:

```http
GET /api/algorithms/nearest-bus

