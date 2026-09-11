# Algoritmo de Dijkstra

## Objetivo

Encontrar el camino de menor costo desde un nodo de origen hacia los demás nodos de un grafo, utilizando pesos no negativos en las conexiones.

En SmartRoute GT, este algoritmo puede utilizarse para analizar recorridos entre puntos de una red de transporte y determinar una alternativa de menor costo, distancia o tiempo cuando estos valores se representan como pesos del grafo.

## Explicación

Dijkstra es un algoritmo para encontrar caminos mínimos en un grafo ponderado.

El algoritmo comienza seleccionando un nodo de origen. A este nodo se le asigna una distancia inicial de cero, mientras que a los demás nodos se les asigna una distancia inicialmente infinita.

En cada iteración se selecciona el nodo no visitado que tenga la menor distancia conocida. Después se revisan sus vecinos y se calcula si pasar por el nodo actual permite obtener una distancia menor.

Si se encuentra una distancia menor, se actualiza la distancia del vecino y se registra el nodo anterior. El proceso continúa hasta que no quedan nodos pendientes o se alcanza el destino buscado.

Dijkstra funciona correctamente cuando los pesos de las aristas son mayores o iguales a cero.

## Pseudocódigo

```text
Dijkstra(G, origen)

    Para cada nodo v en G:
        distancia[v] = infinito
        anterior[v] = NULL
        visitado[v] = falso

    distancia[origen] = 0

    Mientras existan nodos no visitados:

        u = nodo no visitado con menor distancia

        Si u no existe:
            terminar

        visitado[u] = verdadero

        Para cada vecino v de u:

            nuevaDistancia = distancia[u] + peso(u, v)

            Si nuevaDistancia < distancia[v]:
                distancia[v] = nuevaDistancia
                anterior[v] = u

    Retornar distancia y anterior
