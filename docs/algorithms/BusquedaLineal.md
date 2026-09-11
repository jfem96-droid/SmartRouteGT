# Algoritmo de Búsqueda Lineal

## Objetivo

Encontrar un elemento dentro de una colección de datos recorriendo sus elementos uno por uno hasta encontrar el valor buscado o llegar al final de la colección.

## Explicación

La búsqueda lineal, también llamada búsqueda secuencial, revisa los elementos de una colección en orden.

El algoritmo comienza con el primer elemento y lo compara con el valor que se desea encontrar. Si coinciden, la búsqueda termina y se devuelve la posición encontrada.

Si no coinciden, continúa con el siguiente elemento hasta encontrar el valor o llegar al final de la colección.

Una ventaja de este algoritmo es que no requiere que los datos estén ordenados.

## Pseudocódigo

```text
BusquedaLineal(A, objetivo)

    Para i desde 0 hasta longitud(A) - 1:

        Si A[i] == objetivo:
            Retornar i

    Retornar -1

## Complejidad temporal

En el peor caso, el algoritmo debe revisar todos los elementos.

La complejidad temporal es:

**O(n)**

En el mejor caso, el elemento buscado se encuentra en la primera posición:

**O(1)**

donde `n` representa la cantidad de elementos de la colección.

## Complejidad espacial

La búsqueda lineal utiliza una cantidad constante de memoria adicional.

La complejidad espacial es:

**O(1)**

## Justificación de uso en SmartRoute GT

La búsqueda lineal puede ser útil en SmartRoute GT cuando se necesita localizar información dentro de una colección pequeña o cuando los datos no están ordenados.

Por ejemplo, puede utilizarse conceptualmente para buscar:

- Un bus dentro de una lista.
- Un piloto mediante un identificador.
- Una parada dentro de una colección.
- Una ruta determinada.
- Un elemento específico dentro de una lista de registros.

Su principal ventaja es que puede trabajar directamente con datos que no necesitan estar ordenados previamente.

> **Nota:** Esta documentación describe la aplicación de la búsqueda lineal dentro del contexto de SmartRoute GT. No implica que el algoritmo ya esté implementado en el código Java del proyecto.

## Diagrama de flujo

```text
                  ┌─────────────┐
                  │    Inicio   │
                  └──────┬──────┘
                         │
                         ▼
              ┌────────────────────┐
              │ Seleccionar primer │
              │ elemento           │
              └─────────┬──────────┘
                        │
                        ▼
              ┌────────────────────┐
              │ ¿Elemento actual   │
              │ es el buscado?     │
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
              │ ¿Quedan elementos? │
              └─────────┬──────────┘
                   No   │   Sí
                        │
                        ▼
              ┌────────────────────┐
              │ Pasar al siguiente │
              │ elemento           │
              └─────────┬──────────┘
                        │
                        └──────────────┐
                                       ▼
                              Comparar elemento
