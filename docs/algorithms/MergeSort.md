# Algoritmo Merge Sort

## Objetivo

Ordenar una colección de elementos dividiéndola en partes más pequeñas, ordenando cada parte y posteriormente combinándolas de forma ordenada.

## Explicación

Merge Sort es un algoritmo de ordenamiento basado en la estrategia **divide y vencerás**.

Primero, la colección se divide en dos partes aproximadamente iguales. Cada parte se vuelve a dividir hasta obtener colecciones de un solo elemento.

Después, las partes se combinan comparando sus elementos y colocándolos en el orden correspondiente.

Este proceso continúa hasta obtener nuevamente una sola colección completamente ordenada.

Una de sus principales ventajas es que mantiene una complejidad temporal de O(n log n) incluso en el peor caso.

## Pseudocódigo

```text
MergeSort(A)

    Si longitud(A) <= 1:
        Retornar A

    medio = longitud(A) / 2

    izquierda = MergeSort(A[0 : medio])
    derecha = MergeSort(A[medio : fin])

    Retornar Mezclar(izquierda, derecha)


Mezclar(izquierda, derecha)

    Crear resultado vacío

    Mientras izquierda y derecha tengan elementos:

        Si primer elemento de izquierda
        <= primer elemento de derecha:

            Agregar primer elemento de izquierda
            a resultado

        Si no:

            Agregar primer elemento de derecha
            a resultado

    Agregar los elementos restantes de izquierda

    Agregar los elementos restantes de derecha

    Retornar resultado

## Complejidad temporal

Merge Sort divide la colección en partes y posteriormente realiza el proceso de combinación.

La complejidad temporal es:

**O(n log n)**

Esta complejidad se mantiene en:

- Mejor caso: **O(n log n)**
- Caso promedio: **O(n log n)**
- Peor caso: **O(n log n)**

donde `n` representa la cantidad de elementos.

## Complejidad espacial

En una implementación típica utilizando arreglos auxiliares, Merge Sort necesita memoria adicional para realizar las combinaciones.

La complejidad espacial adicional es:

**O(n)**

La pila de llamadas recursivas utiliza aproximadamente **O(log n)**, pero el almacenamiento auxiliar utilizado para las combinaciones hace que la complejidad espacial total típica sea **O(n)**.

## Justificación de uso en SmartRoute GT

Merge Sort puede utilizarse conceptualmente en SmartRoute GT para ordenar grandes cantidades de información de manera eficiente.

Por ejemplo, podría utilizarse para ordenar:

- Buses por identificador.
- Rutas según un criterio determinado.
- Paradas por identificador o nombre.
- Registros relacionados con el sistema de transporte.
- Información que necesite organizarse antes de ser presentada.

Su complejidad **O(n log n)** lo hace más apropiado que algoritmos como Bubble Sort cuando se trabaja con colecciones grandes.

> **Nota:** Esta documentación describe la aplicación de Merge Sort dentro del contexto de SmartRoute GT. No implica que el algoritmo ya esté implementado en el código Java del proyecto.

## Diagrama de flujo

                  ┌─────────────┐
                  │    Inicio   │
                  └──────┬──────┘
                         │
                         ▼
              ┌────────────────────┐
              │ Recibir colección  │
              │ de elementos       │
              └─────────┬──────────┘
                        │
                        ▼
              ┌────────────────────┐
              │ ¿Tiene 0 o 1       │
              │ elemento?          │
              └─────────┬──────────┘
                   Sí   │   No
                    │   │
                    ▼   ▼
        ┌────────────────────┐
        │ Retornar colección │
        │ y finalizar        │
        └──────────┬─────────┘
                   │
                   ▼
                 ┌─────┐
                 │ Fin │
                 └─────┘


              Si tiene más de un elemento:

                        │
                        ▼
              ┌────────────────────┐
              │ Dividir colección  │
              │ en dos partes      │
              └─────────┬──────────┘
                        │
                 ┌──────┴──────┐
                 ▼             ▼
       ┌────────────────┐ ┌────────────────┐
       │ Ordenar primera│ │ Ordenar segunda│
       │ mitad          │ │ mitad          │
       │ con MergeSort  │ │ con MergeSort  │
       └───────┬────────┘ └───────┬────────┘
               │                  │
               └────────┬─────────┘
                        ▼
              ┌────────────────────┐
              │ Combinar las dos   │
              │ partes ordenadas   │
              └─────────┬──────────┘
                        │
                        ▼
              ┌────────────────────┐
              │ Retornar colección │
              │ ordenada           │
              └─────────┬──────────┘
                        │
                        ▼
                   ┌─────────┐
                   │   Fin   │
                   └─────────┘
