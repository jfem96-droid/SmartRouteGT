# Módulo DriverRouteAssignment

## Descripción

El módulo `DriverRouteAssignment` representa la asignación de un piloto (`Driver`), un bus (`Bus`) y una ruta (`Route`).

La entidad se encuentra en:

`backend/src/main/java/com/smartroutegt/backend/entity/DriverRouteAssignment.java`

## Entidad DriverRouteAssignment

La entidad está marcada con `@Entity`, por lo que representa una entidad persistente de JPA.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | Identificador de la asignación. Se genera automáticamente mediante `GenerationType.IDENTITY`. |
| `driver` | `Driver` | Piloto asociado a la asignación. |
| `bus` | `Bus` | Bus asociado a la asignación. |
| `route` | `Route` | Ruta asociada a la asignación. |
| `activa` | `boolean` | Indica el estado de la asignación. Su valor inicial es `true`. |

### Relaciones

La entidad utiliza relaciones `@ManyToOne`:

- `driver` se relaciona con `Driver` mediante la columna `driver_id`.
- `bus` se relaciona con `Bus` mediante la columna `bus_id`.
- `route` se relaciona con `Route` mediante la columna `route_id`.

Las tres relaciones tienen `nullable = false`, por lo que requieren un valor.

## Constructor

La entidad dispone de un constructor vacío requerido para la persistencia mediante JPA.

También dispone de un constructor que recibe:

- `Driver driver`
- `Bus bus`
- `Route route`

Al utilizar este constructor, la asignación se inicializa como activa.

## Métodos

La entidad proporciona métodos getter y setter para sus atributos modificables.

- `getId()` obtiene el identificador.
- `getDriver()` y `setDriver()` consultan y modifican el piloto.
- `getBus()` y `setBus()` consultan y modifican el bus.
- `getRoute()` y `setRoute()` consultan y modifican la ruta.
- `isActiva()` y `setActiva()` consultan y modifican el estado de la asignación.

## Repositorio

El repositorio se encuentra en:

`backend/src/main/java/com/smartroutegt/backend/repository/DriverRouteAssignmentRepository.java`

`DriverRouteAssignmentRepository` extiende `JpaRepository<DriverRouteAssignment, Long>`.

Esto proporciona las operaciones estándar de persistencia de Spring Data JPA para la entidad `DriverRouteAssignment`, utilizando `Long` como tipo del identificador.

Actualmente, el repositorio no declara consultas personalizadas.

## Alcance actual

Esta documentación describe únicamente la entidad y el repositorio que existen actualmente en el módulo.

Los controladores y consultas específicas de asignaciones corresponden a otras tareas del equipo y no se documentan aquí como implementaciones existentes.
