# QA Final — SmartRoute GT

## 1. Objetivo

Esta documentación registra las pruebas realizadas sobre el proyecto **SmartRoute GT** durante la fase final de QA.

El objetivo fue verificar el funcionamiento del backend, la conexión con MySQL, el panel administrativo, el CRUD de rutas, el monitoreo de buses, las validaciones de formularios, el comportamiento de la aplicación cuando el backend está desconectado y la consola del navegador.

La fase de QA se realizó procurando no modificar funcionalidades importantes durante las pruebas. Las incidencias encontradas quedaron documentadas para su atención posterior.

---

## 2. Entorno de pruebas

- **Sistema operativo:** Windows 11
- **Java:** 21.0.12.1
- **Spring Boot:** 4.1.1
- **Maven:** 3.9.16
- **MySQL:** 8.0.46
- **Base de datos:** `smartroute_gt`
- **Backend:** Spring Boot REST API
- **Puerto del backend:** `8080`
- **Frontend administrativo:** HTML, CSS, JavaScript y Bootstrap
- **Control de versiones:** Git/GitHub

### Configuración de base de datos

El backend utiliza la variable de entorno `DB_PASSWORD` para obtener la contraseña de MySQL.

Por seguridad, **la contraseña real no debe almacenarse en este documento ni subirse al repositorio**.

Ejemplo de configuración para ejecutar el backend:

```bash
export DB_PASSWORD='CONTRASEÑA_REAL_DE_MYSQL'
mvn spring-boot:run
```

---

## 3. Arquitectura general

SmartRoute GT utiliza una arquitectura basada en aplicaciones cliente, una API REST y una base de datos.

```text
┌──────────────────────────────┐
│       Aplicación Android     │
│       Piloto / Pasajero      │
│            Kotlin            │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│          Backend             │
│     Java + Spring Boot       │
│         REST API             │
│          Puerto 8080         │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│            MySQL             │
│        smartroute_gt         │
└──────────────────────────────┘

┌──────────────────────────────┐
│         Admin Web            │
│ HTML + CSS + JavaScript      │
│          Bootstrap           │
└──────────────┬───────────────┘
               │
               ▼
          Backend REST API
```

---

## 4. Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 21 | Desarrollo del backend |
| Spring Boot | API REST y lógica del servidor |
| Spring Data JPA | Persistencia de datos |
| Hibernate | ORM |
| Maven | Gestión y construcción del proyecto |
| MySQL 8.0.46 | Base de datos |
| HTML | Estructura del administrador web |
| CSS | Estilos |
| JavaScript | Lógica del administrador web |
| Bootstrap | Diseño de interfaz |
| Android/Kotlin | Aplicaciones móviles |
| Git/GitHub | Control de versiones |

---

## 5. Funcionalidades del proyecto

SmartRoute GT está orientado al monitoreo y administración del transporte público.

Entre las funcionalidades contempladas por el proyecto se encuentran:

- Administración de rutas.
- Administración de paradas.
- Administración de buses.
- Administración de pilotos.
- Asignación de piloto, bus y ruta.
- Monitoreo de buses.
- Ubicación mediante GPS.
- Control de velocidad.
- Gestión de infracciones y sanciones.
- Alertas de pasajeros.
- Reportes.
- Administración mediante panel web.

Las funcionalidades deben distinguirse entre las que ya están implementadas y las que todavía se encuentran en desarrollo.

---

## 6. Panel administrativo

Durante la fase de QA se verificaron principalmente las siguientes áreas:

### 6.1 Dashboard

Se verificó que el dashboard administrativo cargara correctamente.

Los indicadores observados fueron:

- Buses activos.
- Pilotos activos.
- Alertas pendientes.

Cuando no existen registros correspondientes, se muestra el valor cero.

### 6.2 Rutas

Se verificó el funcionamiento del CRUD de rutas:

- Visualización de la tabla.
- Creación de una ruta.
- Validación del código obligatorio.
- Validación del nombre obligatorio.
- Edición de una ruta.
- Desactivación de una ruta.
- Activación de una ruta.

Las operaciones probadas funcionaron correctamente.

### 6.3 Monitoreo de buses

Se verificó que:

- La tabla de monitoreo sea visible.
- La información sea solicitada al backend.
- El estado vacío sea mostrado correctamente cuando no existen buses registrados.

El estado `No hay buses registrados` se considera un estado válido cuando la base de datos no contiene registros de buses.

---

# 7. Pruebas de QA

## QA-01 | Backend + MySQL | FUNCIONA | Baja

**Descripción:**  
El backend requiere la variable `DB_PASSWORD` para conectarse correctamente a MySQL. Con la variable configurada con la contraseña real, Spring Boot inicia correctamente, Hibernate establece la conexión y Tomcat queda disponible en el puerto 8080.

**Pasos para reproducir:**

1. Abrir Git Bash.
2. Entrar en la carpeta `backend`.
3. Configurar `DB_PASSWORD` con la contraseña real de MySQL.
4. Ejecutar:

```bash
mvn spring-boot:run
```

5. Verificar los mensajes de inicio.
6. Confirmar la conexión con la base de datos `smartroute_gt`.

**Resultado obtenido:**

- Conexión a MySQL exitosa.
- Base de datos `smartroute_gt` accesible.
- Tomcat iniciado en el puerto 8080.
- `Started BackendApplication` confirmado.

---

## QA-02 | Navegación del menú | NO FUNCIONA | Media

**Descripción:**  
Los elementos del menú administrativo se muestran en la interfaz, pero algunas opciones no realizan el cambio de sección o navegación correspondiente.

**Opciones observadas:**

- Dashboard
- Mapa en vivo
- Rutas
- Paradas
- Buses
- Pilotos
- Sanciones
- Alertas de pasajeros
- Reportes
- Auditoría

Durante la prueba, al seleccionar determinadas opciones del menú, la interfaz permaneció en la sección actual.

**Pasos para reproducir:**

1. Abrir `admin-web/index.html`.
2. Encender el backend.
3. Seleccionar las diferentes opciones del menú.
4. Verificar si la sección cambia.
5. Comprobar que algunas opciones no realizan la navegación esperada.

**Resultado obtenido:**

La navegación de determinadas opciones queda registrada como incidencia pendiente.

**Acción:**  
No se modificó código durante la fase de QA para solucionar esta incidencia.

---

## QA-03 | Backend desconectado | FUNCIONA | Baja

**Descripción:**  
Cuando el backend está apagado, la interfaz muestra mensajes indicando que determinados servicios no pudieron cargarse.

**Pasos para reproducir:**

1. Detener el backend.
2. Abrir o recargar `admin-web/index.html`.
3. Observar las secciones que realizan solicitudes al backend.
4. Verificar los mensajes mostrados.

**Resultado obtenido:**

La interfaz muestra mensajes comprensibles, entre ellos:

- `No se pudieron cargar las rutas`
- `No se pudo cargar el servicio de alertas`
- `No se pudo cargar el monitoreo de buses`

El comportamiento de la interfaz ante la falta de conexión fue identificado y documentado correctamente.

---

## QA-04 | Consola con backend desconectado | ERROR REGISTRADO | Media

**Descripción:**  
Con el backend apagado, el navegador registra errores de red relacionados con las solicitudes que intentan acceder a `localhost:8080`.

Los mensajes incluyen errores como `NetworkError when attempting to fetch resource` y mensajes del navegador relacionados con solicitudes CORS.

**Importante:**  
Estos mensajes fueron observados con el backend apagado. No se consideran por sí solos evidencia de un problema de configuración CORS del backend. Para clasificar un problema real de CORS sería necesario reproducirlo con el backend funcionando.

**Pasos para reproducir:**

1. Detener el backend.
2. Abrir las herramientas de desarrollador con `F12`.
3. Entrar en la pestaña `Console`.
4. Recargar la página.
5. Observar los errores generados por las solicitudes al backend.

**Resultado obtenido:**

Se registran errores de red debido a que el servicio backend no está disponible.

---

## QA-05 | CRUD de Rutas | FUNCIONA | Sin incidencia

**Descripción:**  
Se verificó el funcionamiento de las operaciones principales del CRUD de rutas.

**Pruebas realizadas:**

- Crear ruta.
- Editar ruta.
- Desactivar ruta.
- Activar ruta.
- Validar código obligatorio.
- Validar nombre obligatorio.

**Pasos para reproducir:**

1. Abrir el panel administrativo.
2. Entrar a la sección `Rutas`.
3. Seleccionar `Nueva ruta`.
4. Intentar guardar con el código vacío.
5. Intentar guardar con el nombre vacío.
6. Crear una ruta válida.
7. Editar la ruta creada.
8. Desactivarla.
9. Activarla nuevamente.

**Resultado obtenido:**

Todas las operaciones probadas funcionaron correctamente.

---

## QA-06 | Monitoreo de buses | FUNCIONA | Sin incidencia

**Descripción:**  
Se verificó la carga de la sección de monitoreo de buses y la consulta de información al backend.

**Pasos para reproducir:**

1. Encender el backend.
2. Abrir el panel administrativo.
3. Recargar la página.
4. Observar la sección `Monitoreo de buses`.
5. Verificar la tabla.

**Resultado obtenido:**

La tabla de monitoreo se muestra correctamente.

Cuando no existen buses registrados, se presenta el estado vacío correspondiente:

`No hay buses registrados`.

Este estado no se considera un error.

---

## QA-07 | Consola con backend encendido | SIN ERRORES | Sin incidencia

**Descripción:**  
Se verificó el comportamiento de la consola del navegador después de iniciar correctamente el backend.

**Pasos para reproducir:**

1. Encender el backend.
2. Abrir `admin-web/index.html`.
3. Presionar `Ctrl + R`.
4. Abrir las herramientas de desarrollador con `F12`.
5. Revisar la pestaña `Console`.

**Resultado obtenido:**

- La tabla de rutas apareció correctamente.
- La tabla de monitoreo de buses apareció correctamente.
- No se observaron errores rojos en la consola mientras el backend estaba funcionando.

---

# 8. Resumen de resultados

| ID | Módulo | Resultado | Severidad |
|---|---|---|---|
| QA-01 | Backend + MySQL | FUNCIONA | Baja |
| QA-02 | Navegación del menú | NO FUNCIONA | Media |
| QA-03 | Backend desconectado | FUNCIONA | Baja |
| QA-04 | Consola con backend desconectado | ERROR REGISTRADO | Media |
| QA-05 | CRUD de Rutas | FUNCIONA | — |
| QA-06 | Monitoreo de buses | FUNCIONA | — |
| QA-07 | Consola con backend encendido | SIN ERRORES | — |

---

# 9. Incidencias encontradas

La principal incidencia funcional encontrada durante la fase de QA corresponde a la navegación del menú administrativo.

Las opciones del menú están visibles, pero determinadas opciones todavía no realizan el cambio de sección correspondiente.

Esta incidencia queda registrada para una futura tarea de desarrollo.

Durante la fase de QA no se realizaron cambios de código para corregirla, debido a que el objetivo de esta etapa fue identificar, reproducir y documentar los problemas.

---

# 10. Algoritmos documentados

El proyecto cuenta con documentación de algoritmos relacionados con posibles necesidades de SmartRoute GT:

- Dijkstra.
- BFS.
- DFS.
- Búsqueda lineal.
- Búsqueda binaria.
- Bubble Sort.
- Merge Sort.
- Greedy.

La documentación de estos algoritmos incluye:

- Objetivo.
- Explicación.
- Pseudocódigo.
- Complejidad temporal.
- Complejidad espacial.
- Justificación de uso dentro de SmartRoute GT.
- Diagramas o flujogramas correspondientes.

---

# 11. Ejecución del proyecto

## Backend

Desde Git Bash:

```bash
cd SmartRouteGT/backend
export DB_PASSWORD='CONTRASEÑA_REAL_DE_MYSQL'
mvn spring-boot:run
```

Una ejecución correcta debe mostrar mensajes similares a:

```text
Tomcat started on port 8080
Started BackendApplication
```

## Admin Web

El administrador web se encuentra en:

```text
SmartRouteGT/admin-web/
```

El archivo principal es:

```text
admin-web/index.html
```

---

# 12. Seguridad de configuración

No se deben subir al repositorio:

- Contraseñas de MySQL.
- Tokens.
- Claves privadas.
- Credenciales de servicios.

La contraseña de MySQL utilizada durante las pruebas se proporcionó mediante la variable de entorno `DB_PASSWORD` y no forma parte de esta documentación.

---

# 13. Conclusión

La fase final de QA de SmartRoute GT permitió verificar el funcionamiento del backend, la conexión con MySQL, el panel administrativo, el CRUD de rutas, el monitoreo de buses y el comportamiento de la interfaz ante la desconexión del backend.

Las operaciones principales del CRUD de rutas fueron ejecutadas correctamente, incluyendo validaciones, creación, edición, activación y desactivación.

El monitoreo de buses también fue verificado y mostró correctamente el estado vacío cuando no existen registros.

Se identificó una incidencia relacionada con la navegación de determinadas opciones del menú administrativo. Esta incidencia quedó documentada para una futura etapa de desarrollo.

## Estado final

**FASE DE QA: FINALIZADA**

**Cambios de código realizados durante QA:** Ninguno.

**Incidencia funcional pendiente:** Navegación de determinadas opciones del menú administrativo.

**Resultado general:** Las funcionalidades que fueron efectivamente probadas durante esta fase respondieron de acuerdo con los escenarios establecidos, dejando documentada la incidencia de navegación para su posterior atención.
