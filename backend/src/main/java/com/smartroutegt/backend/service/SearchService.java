package com.smartroutegt.backend.service;

import com.smartroutegt.backend.entity.Bus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SearchService {

    public Bus busquedaLineal(List<Bus> buses, String codigo) {

        for (Bus bus : buses) {

            if (bus.getCodigo().equalsIgnoreCase(codigo)) {
                return bus;
            }
        }

        return null;
    }

    public Bus busquedaBinaria(List<Bus> buses, String codigo) {

        List<Bus> busesOrdenados = new ArrayList<>(buses);

        busesOrdenados.sort(
                Comparator.comparing(
                        Bus::getCodigo,
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        int izquierda = 0;
        int derecha = busesOrdenados.size() - 1;

        while (izquierda <= derecha) {

            int medio = izquierda + (derecha - izquierda) / 2;

            Bus busMedio = busesOrdenados.get(medio);

            int comparacion =
                    busMedio.getCodigo().compareToIgnoreCase(codigo);

            if (comparacion == 0) {
                return busMedio;
            }

            if (comparacion < 0) {
                izquierda = medio + 1;
            } else {
                derecha = medio - 1;
            }
        }

        return null;
    }
}