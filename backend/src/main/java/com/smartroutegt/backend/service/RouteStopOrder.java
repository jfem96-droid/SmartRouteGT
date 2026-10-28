package com.smartroutegt.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Búsqueda dirigida: no invierte rutas ni inventa paradas. */
public final class RouteStopOrder {
    private RouteStopOrder() {}
    public static List<Long> portion(List<Long> orderedIds, Long origin, Long destination) {
        if (origin == null || destination == null || Objects.equals(origin, destination)) return List.of();
        for (int i = 0; i < orderedIds.size(); i++) {
            if (!origin.equals(orderedIds.get(i))) continue;
            for (int j = i + 1; j < orderedIds.size(); j++) {
                if (destination.equals(orderedIds.get(j))) {
                    return new ArrayList<>(orderedIds.subList(i, j + 1));
                }
            }
        }
        return List.of();
    }
}
