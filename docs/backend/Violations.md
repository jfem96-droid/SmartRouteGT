# Módulo de Infracciones

## 1. Objetivo

El módulo de infracciones permite trabajar con la información de sanciones asociadas a los pilotos del sistema SmartRoute GT.

Actualmente, el backend cuenta con información de sanciones confirmadas asociada al conductor y una funcionalidad para ordenar los pilotos según la cantidad de sanciones.

---

## 2. Funcionamiento

El sistema utiliza el campo de sanciones confirmadas del conductor para determinar la cantidad de sanciones asociadas a cada piloto.

La información puede utilizarse para ordenar los conductores y facilitar la identificación de aquellos que tienen mayor cantidad de sanciones.

---

## 3. Funcionalidad implementada

Actualmente existe la consulta:

```http
GET /api/drivers/ordenados-por-sanciones

