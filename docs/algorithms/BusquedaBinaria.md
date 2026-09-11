# Algoritmo de Búsqueda Binaria

## Objetivo

Encontrar rápidamente un elemento dentro de una colección de datos que se encuentra ordenada.

La búsqueda binaria reduce el espacio de búsqueda a la mitad en cada comparación.

## Explicación

La búsqueda binaria es un algoritmo de búsqueda eficiente que requiere que los datos estén ordenados.

El algoritmo compara el elemento buscado con el elemento que se encuentra en la posición central de la colección.

Si el valor buscado es igual al elemento central, la búsqueda termina.

Si el valor buscado es menor, se continúa buscando en la mitad izquierda.

Si el valor buscado es mayor, se continúa buscando en la mitad derecha.

Este proceso se repite hasta encontrar el elemento o hasta que no queden elementos por revisar.

## Pseudocódigo

```text
BusquedaBinaria(A, objetivo)

    inicio = 0
    fin = longitud(A) - 1

    Mientras inicio <= fin:

        medio = (inicio + fin) / 2

        Si A[medio] == objetivo:
            Retornar medio

        Si A[medio] < objetivo:
            inicio = medio + 1

        Si A[medio] > objetivo:
            fin = medio - 1

    Retornar -1

## Complejidad temporal

La búsqueda binaria divide el espacio de búsqueda aproximadamente a la mitad en cada iteración.

La complejidad temporal es:

**O(log n)**

En el mejor caso, cuando el elemento se encuentra en la primera comparación, la complejidad es:

**O(1)**

donde `n` representa la cantidad de elementos.

## Complejidad espacial

La versión iterativa utiliza una cantidad constante de memoria adicional.

La complejidad espacial es:

**O(1)**

## Justificación de uso en SmartRoute GT

La búsqueda binaria puede ser útil en SmartRoute GT cuando se dispone de información ordenada y se necesita localizar rápidamente un elemento.

Por ejemplo, puede utilizarse conceptualmente para buscar:

- Un bus dentro de una lista ordenada por identificador.
- Un piloto mediante un identificador ordenado.
- Una parada dentro de una lista ordenada.
- Una ruta mediante su identificador.
- Registros organizados por algún valor numérico.

Su principal ventaja frente a la búsqueda lineal es que reduce el espacio de búsqueda a la mitad en cada paso.

> **Nota:** Esta documentación describe la aplicación de la búsqueda binaria dentro del contexto de SmartRoute GT. No implica que el algoritmo ya esté implementado en el código Java del proyecto.

## Diagrama de flujo

```text
                  ┌─────────────┐
                  │    Inicio   │
                  └──────┬──────┘
                         │
                         ▼
              ┌────────────────────┐
              │ Definir inicio y   │
              │ fin                │
              └─────────┬──────────┘
                        │
                        ▼
              ┌────────────────────┐
              │ Calcular posición  │
              │ central            │
              └─────────┬──────────┘
                        │
                        ▼
              ┌────────────────────┐
              │ ¿A[medio] es el    │
              │ elemento buscado?  │
              └─────────┬──────────┘
                   Sí   │   No
                        │
                        ▼
              ┌────────────────────┐
              │ Retornar posición  │
              │ y finalizar        │
              └────────────────────┘

Si no coincide:

                        │
                        ▼
              ┌────────────────────┐
              │ ¿A[medio] <        │
              │ objetivo?          │
              └─────────┬──────────┘
                   Sí   │   No
                        │
                        ▼
              ┌────────────────────┐
              │ Buscar en la mitad │
              │ derecha            │
              └─────────┬──────────┘
                        │
                        ▼
                  Repetir búsqueda

Si A[medio] > objetivo:

              ┌────────────────────┐
              │ Buscar en la mitad │
              │ izquierda          │
              └─────────┬──────────┘
                        │
                        ▼
                  Repetir búsqueda
