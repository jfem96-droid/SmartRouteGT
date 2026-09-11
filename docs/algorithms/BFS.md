# Algoritmo BFS (Breadth-First Search)

## Objetivo

Recorrer y explorar un grafo por niveles, visitando primero los nodos que se encuentran más cerca del nodo de origen.

BFS puede utilizarse para encontrar el número mínimo de conexiones entre dos nodos cuando todas las conexiones tienen el mismo costo.

## Explicación

BFS, cuyo nombre significa **Breadth-First Search**, es un algoritmo de recorrido de grafos que explora los nodos por niveles.

El algoritmo comienza en un nodo de origen y lo agrega a una cola. Después toma el primer nodo de la cola, visita sus vecinos que todavía no han sido explorados y los agrega a la cola.

Este proceso continúa hasta que la cola queda vacía o hasta encontrar el nodo que se está buscando.

BFS utiliza una estructura de datos llamada **cola (Queue)**, que sigue el principio FIFO (*First In, First Out*).

## Pseudocódigo

```text
BFS(G, origen)

    Crear una cola Q
    Crear conjunto visitado

    Agregar origen a visitado
    Insertar origen en Q

    Mientras Q no esté vacía:

        u = extraer primer elemento de Q

        Procesar u

        Para cada vecino v de u:

            Si v no está visitado:

                Agregar v a visitado
                Insertar v en Q

    Finalizar recorrido

## Complejidad temporal

Con una representación del grafo mediante lista de adyacencia, BFS visita cada vértice y cada arista como máximo una vez.

La complejidad temporal es:

**O(V + E)**

donde:

- `V` = cantidad de vértices.
- `E` = cantidad de aristas.

## Complejidad espacial

BFS necesita almacenar los nodos visitados y una cola con los nodos pendientes.

La complejidad espacial es:

**O(V)**

en términos de almacenamiento adicional para el recorrido.

## Justificación de uso en SmartRoute GT

SmartRoute GT trabaja con rutas, paradas y conexiones entre diferentes puntos del sistema de transporte.

BFS puede utilizarse para explorar una red de paradas por niveles y determinar el camino con menor número de conexiones cuando todas las conexiones tienen el mismo costo.

Por ejemplo, si una ruta se representa como un grafo donde las paradas son nodos y las conexiones entre ellas son aristas, BFS puede ayudar a determinar cuántas conexiones son necesarias para llegar desde una parada de origen hasta una parada de destino.

También puede ser útil para:

- Explorar paradas cercanas dentro de una red.
- Recorrer una estructura de rutas.
- Encontrar un destino con el menor número de conexiones.
- Verificar qué elementos de una red son alcanzables desde un punto determinado.

> **Nota:** Esta documentación describe la aplicación de BFS dentro del contexto de SmartRoute GT. No implica que el algoritmo ya esté implementado en el código Java del proyecto.

## Diagrama de flujo

```text
                  ┌─────────────┐
                  │    Inicio   │
                  └──────┬──────┘
                         │
                         ▼
              ┌─────────────────────┐
              │ Crear cola y        │
              │ conjunto visitado   │
              └──────────┬──────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │ Agregar nodo origen │
              │ a la cola           │
              └──────────┬──────────┘
                         │
                         ▼
                  ┌──────────────┐
                  │ ¿Cola vacía? │
                  └──────┬───────┘
                     Sí  │  No
                         │
                         ▼
                   ┌───────────┐
                   │    Fin    │
                   └───────────┘

Si la cola no está vacía:

                         │
                         ▼
              ┌────────────────────┐
              │ Extraer primer     │
              │ nodo de la cola    │
              └─────────┬──────────┘
                        │
                        ▼
              ┌────────────────────┐
              │ Revisar sus        │
              │ vecinos            │
              └─────────┬──────────┘
                        │
                        ▼
              ┌────────────────────┐
              │ ¿Vecino no         │
              │ visitado?          │
              └─────────┬──────────┘
                   No   │   Sí
                        │
                        ▼
              ┌────────────────────┐
              │ Marcar como        │
              │ visitado y agregar │
              │ a la cola          │
              └─────────┬──────────┘
                        │
                        └──────────────┐
                                       ▼
                              ¿Cola vacía?
