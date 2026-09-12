# API del Backend - SmartRoute GT

## 1. Objetivo

Este documento describe los endpoints relacionados con algoritmos y búsquedas implementados actualmente en el backend de SmartRoute GT.

La documentación se basa únicamente en los controladores y servicios existentes en el proyecto.

## 2. Algoritmos de grafos

### 2.1 Agregar conexión

**Endpoint:**

POST /api/algorithms/connection

**Parámetros:**

- `origen`: ID del nodo de origen.
- `destino`: ID del nodo destino.
- `peso`: peso de la conexión.

**Ejemplo:**

```bash
curl -X POST "http://localhost:8080/api/algorithms/connection?origen=1&destino=2&peso=5"

