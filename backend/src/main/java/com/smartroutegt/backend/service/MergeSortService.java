package com.smartroutegt.backend.service;

import com.smartroutegt.backend.entity.Bus;
import com.smartroutegt.backend.repository.BusRepository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MergeSortService {

    private final BusRepository busRepository;

    public MergeSortService(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    public List<Bus> obtenerBusesOrdenadosPorCapacidad() {

        List<Bus> buses =
                new ArrayList<>(busRepository.findAll());

        return mergeSort(buses);
    }

    private List<Bus> mergeSort(List<Bus> buses) {

        if (buses.size() <= 1) {
            return new ArrayList<>(buses);
        }

        int mitad = buses.size() / 2;

        List<Bus> izquierda = new ArrayList<>();
        List<Bus> derecha = new ArrayList<>();

        for (int i = 0; i < mitad; i++) {
            izquierda.add(buses.get(i));
        }

        for (int i = mitad; i < buses.size(); i++) {
            derecha.add(buses.get(i));
        }

        izquierda = mergeSort(izquierda);
        derecha = mergeSort(derecha);

        return merge(izquierda, derecha);
    }

    private List<Bus> merge(
            List<Bus> izquierda,
            List<Bus> derecha) {

        List<Bus> resultado = new ArrayList<>();

        int i = 0;
        int j = 0;

        while (i < izquierda.size()
                && j < derecha.size()) {

            if (izquierda.get(i).getCapacidad()
                    <= derecha.get(j).getCapacidad()) {

                resultado.add(izquierda.get(i));
                i++;

            } else {

                resultado.add(derecha.get(j));
                j++;
            }
        }

        while (i < izquierda.size()) {
            resultado.add(izquierda.get(i));
            i++;
        }

        while (j < derecha.size()) {
            resultado.add(derecha.get(j));
            j++;
        }

        return resultado;
    }
}