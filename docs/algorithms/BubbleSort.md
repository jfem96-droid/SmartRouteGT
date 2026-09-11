# Algoritmo Bubble Sort

## Objetivo

Ordenar una colección de elementos comparando elementos adyacentes e intercambiándolos cuando se encuentran en el orden incorrecto.

## Explicación

Bubble Sort, también conocido como ordenamiento de burbuja, es un algoritmo de ordenamiento sencillo.

El algoritmo recorre la colección y compara elementos que se encuentran uno junto al otro. Si están en el orden incorrecto, se intercambian.

Después de cada recorrido, el elemento más grande de la parte no ordenada queda colocado en su posición correspondiente.

El proceso se repite hasta que no sea necesario realizar más intercambios.

## Pseudocódigo

```text
BubbleSort(A)

    n = longitud(A)

    Para i desde 0 hasta n - 1:

        intercambio = falso

        Para j desde 0 hasta n - i - 2:

            Si A[j] > A[j + 1]:

                Intercambiar A[j] con A[j + 1]

                intercambio = verdadero

        Si intercambio == falso:
            terminar

    Retornar A

## Complejidad temporal

En el peor caso, cuando los elementos están en orden inverso, Bubble Sort realiza múltiples comparaciones e intercambios.

La complejidad temporal es:

**O(n²)**

En el caso promedio también es:

**O(n²)**

Cuando se utiliza la optimización de detener el algoritmo si no se realizan intercambios, el mejor caso es:

**O(n)**

donde `n` representa la cantidad de elementos.

## Complejidad espacial

Bubble Sort realiza los intercambios directamente sobre la colección y utiliza una cantidad constante de memoria adicional.

La complejidad espacial es:

**O(1)**

## Justificación de uso en SmartRoute GT

Bubble Sort puede utilizarse conceptualmente en SmartRoute GT para ordenar colecciones pequeñas de información cuando se busca una implementación sencilla.

Por ejemplo, podría utilizarse para ordenar:

- Buses por identificador.
- Paradas por algún valor numérico.
- Rutas según un criterio determinado.
- Registros de tamaño pequeño.
- Información que necesite organizarse antes de ser mostrada.

Aunque Bubble Sort es sencillo de implementar, no es la opción más eficiente para grandes cantidades de datos debido a su complejidad O(n²).

> **Nota:** Esta documentación describe la aplicación de Bubble Sort dentro del contexto de SmartRoute GT. No implica que el algoritmo ya esté implementado en el código Java del proyecto.

## Diagrama de flujo

```text
                  ┌─────────────┐
                  │    Inicio   │
                  └──────┬──────┘
                         │
                         ▼
              ┌────────────────────┐
              │ Recorrer elementos │
              │ de la colección    │
              └─────────┬──────────┘
                        │
                        ▼
              ┌────────────────────┐
              │ Comparar elementos │
              │ adyacentes         │
              └─────────┬──────────┘
                        │
                        ▼
              ┌────────────────────┐
              │ ¿Están en orden    │
              │ incorrecto?        │
              └─────────┬──────────┘
                   No   │   Sí
                        │
                        ▼
              ┌────────────────────┐
              │ Intercambiar los   │
              │ elementos          │
              └─────────┬──────────┘
                        │
                        ▼
              ┌────────────────────┐
              │ ¿Quedan elementos  │
              │ por comparar?      │
              └─────────┬──────────┘
                   Sí   │   No
                        │
                        ▼
              ┌────────────────────┐
              │ ¿Hubo intercambios?│
              └─────────┬──────────┘
                   Sí   │   No
                        │
                        ▼
              ┌────────────────────┐
              │ Repetir recorrido  │
              └─────────┬──────────┘
                        │
                        └──────────────┐
                                       ▼
                                ┌───────────┐
                                │    Fin    │
                                └───────────┘
