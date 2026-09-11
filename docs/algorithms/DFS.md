# Algoritmo DFS (Depth-First Search)

## Objetivo

Recorrer y explorar un grafo profundizando en cada camino antes de regresar y explorar otras alternativas.

DFS puede utilizarse para recorrer estructuras de rutas, comprobar la conectividad entre elementos y explorar una red de paradas.

## Explicación

DFS, cuyo nombre significa **Depth-First Search**, es un algoritmo de recorrido de grafos que explora una rama del grafo hasta llegar lo más lejos posible antes de regresar.

El algoritmo comienza en un nodo de origen y lo marca como visitado. Después selecciona un vecino que todavía no haya sido visitado y continúa el recorrido desde ese nodo.

Cuando un nodo no tiene vecinos sin visitar, el algoritmo retrocede hacia el nodo anterior y continúa buscando otras ramas.

DFS puede implementarse utilizando una **pila (Stack)** o mediante **recursividad**.

## Pseudocódigo

```text
DFS(G, origen)

    Crear conjunto visitado

    DFS_Visitar(origen)

DFS_Visitar(u)

    Agregar u a visitado
    Procesar u

    Para cada vecino v de u:

        Si v no está visitado:

            DFS_Visitar(v)

    Regresar

## Complejidad temporal

Con una representación del grafo mediante lista de adyacencia, DFS visita cada vértice y cada arista como máximo una vez.

La complejidad temporal es:

**O(V + E)**

donde:

- `V` = cantidad de vértices.
- `E` = cantidad de aristas.

## Complejidad espacial

DFS necesita almacenar los nodos visitados y la pila de llamadas recursivas o una pila explícita.

La complejidad espacial adicional es:

**O(V)**

En el peor caso, la pila puede contener hasta `V` nodos.

## Justificación de uso en SmartRoute GT

SmartRoute GT trabaja con rutas, paradas y conexiones entre diferentes puntos del sistema de transporte.

DFS puede utilizarse para explorar una red de rutas desde una parada determinada, recorriendo una rama completa antes de regresar y analizar otras conexiones.

Por ejemplo, si las paradas se representan como nodos y las conexiones como aristas, DFS puede ayudar a:

- Explorar una red de paradas.
- Comprobar si existe conexión entre diferentes puntos.
- Recorrer estructuras de rutas.
- Identificar elementos alcanzables desde una parada de origen.
- Analizar diferentes ramas de una red de transporte.

> **Nota:** Esta documentación describe la aplicación de DFS dentro del contexto de SmartRoute GT. No implica que el algoritmo ya esté implementado en el código Java del proyecto.

## Diagrama de flujo

```text
                  ┌─────────────┐
                  │    Inicio   │
                  └──────┬──────┘
                         │
                         ▼
              ┌─────────────────────┐
              │ Crear conjunto      │
              │ de visitados        │
              └──────────┬──────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │ Visitar nodo origen │
              └──────────┬──────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │ Revisar vecinos     │
              │ del nodo actual     │
              └──────────┬──────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │ ¿Hay vecino sin     │
              │ visitar?            │
              └──────────┬──────────┘
                    No   │   Sí
                         │
                         ▼
              ┌─────────────────────┐
              │ Visitar vecino      │
              │ y continuar DFS     │
              └──────────┬──────────┘
                         │
                         └──────────────┐
                                        ▼
                                Revisar vecinos
