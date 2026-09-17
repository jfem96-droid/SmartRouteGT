package com.smartroutegt.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartroutegt.backend.entity.Bus;

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
                (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(
                        a.getCodigo(),
                        b.getCodigo()));

        int izquierda = 0;
        int derecha = busesOrdenados.size() - 1;

        while (izquierda <= derecha) {

            int medio = izquierda + (derecha - izquierda) / 2;

            Bus busMedio = busesOrdenados.get(medio);

            int comparacion = busMedio.getCodigo().compareToIgnoreCase(codigo);

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