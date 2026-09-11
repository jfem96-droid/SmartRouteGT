# Algoritmo Greedy

## Objetivo

Resolver determinados problemas tomando, en cada paso, la alternativa que parece más conveniente en ese momento, con el objetivo de construir una solución global.

El enfoque Greedy puede ser apropiado cuando las decisiones locales permiten obtener una solución óptima para el problema.

## Explicación

Greedy, también conocido como **algoritmo voraz**, es una estrategia que construye una solución paso a paso.

En cada etapa se selecciona la opción que presenta el mejor beneficio inmediato según un criterio determinado.

Una vez tomada una decisión, normalmente no se modifica posteriormente.

Este método puede ser eficiente y sencillo, pero no todos los problemas pueden resolverse correctamente mediante Greedy. Para garantizar una solución óptima, el problema debe cumplir determinadas propiedades que permitan que las mejores decisiones locales conduzcan a una solución global óptima.

## Pseudocódigo

```text
Greedy(Candidatos)

    Crear solución vacía

    Mientras existan candidatos:

        candidato = seleccionar mejor candidato

        Eliminar candidato de los candidatos

        Si candidato es compatible con la solución:

            Agregar candidato a la solución

    Retornar solución

## Complejidad temporal

Greedy no tiene una única complejidad temporal, ya que depende del problema y de la forma en que se seleccionan y procesan los candidatos.

Si los candidatos necesitan ordenarse antes de aplicar la estrategia, la complejidad puede ser:

**O(n log n)**

Si los candidatos ya están ordenados y solamente se realiza un recorrido, puede ser:

**O(n)**

donde `n` representa la cantidad de candidatos.

## Complejidad espacial

La complejidad espacial depende de la implementación.

Si se almacena una lista de candidatos y una solución, puede utilizarse:

**O(n)**

Si el algoritmo trabaja directamente sobre los datos y utiliza únicamente unas pocas variables adicionales, el espacio auxiliar puede ser:

**O(1)**

## Justificación de uso en SmartRoute GT

El enfoque Greedy puede utilizarse conceptualmente en SmartRoute GT para tomar decisiones basadas en un criterio específico en cada paso.

Por ejemplo, podría utilizarse para:

- Seleccionar una alternativa de recorrido con menor tiempo estimado.
- Priorizar determinados buses según un criterio establecido.
- Seleccionar rutas según distancia o costo.
- Organizar tareas según una prioridad.
- Elegir una alternativa disponible que cumpla primero con una condición determinada.

Sin embargo, una decisión localmente óptima no siempre garantiza una solución globalmente óptima. Por esta razón, antes de utilizar Greedy en SmartRoute GT se debe comprobar que el problema permita este tipo de estrategia.

> **Nota:** Esta documentación describe la aplicación del enfoque Greedy dentro del contexto de SmartRoute GT. No implica que el algoritmo ya esté implementado en el código Java del proyecto.

## Diagrama de flujo

                  ┌─────────────┐
                  │    Inicio   │
                  └──────┬──────┘
                         │
                         ▼
              ┌────────────────────┐
              │ Crear solución     │
              │ vacía              │
              └─────────┬──────────┘
                        │
                        ▼
              ┌────────────────────┐
              │ ¿Quedan candidatos?│
              └─────────┬──────────┘
                   No   │   Sí
                    │   │
                    ▼   ▼
              ┌───────────┐
              │    Fin    │
              └───────────┘

              Si quedan candidatos:

                        │
                        ▼
              ┌────────────────────┐
              │ Seleccionar el     │
              │ mejor candidato    │
              └─────────┬──────────┘
                        │
                        ▼
              ┌────────────────────┐
              │ ¿Es compatible     │
              │ con la solución?   │
              └─────────┬──────────┘
                   No   │   Sí
                    │   │
                    │   ▼
                    │ ┌────────────────────┐
                    │ │ Agregar candidato │
                    │ │ a la solución      │
                    │ └─────────┬──────────┘
                    │           │
                    └─────┬─────┘
                          ▼
              ┌────────────────────┐
              │ Eliminar candidato │
              │ de los pendientes  │
              └─────────┬──────────┘
                        │
                        └──────────────┐
                                       ▼
                              ¿Quedan candidatos?

