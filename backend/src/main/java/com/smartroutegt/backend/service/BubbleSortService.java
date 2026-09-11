package com.smartroutegt.backend.service;

import com.smartroutegt.backend.entity.Driver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BubbleSortService {

    public List<Driver> ordenarPorSancionesConfirmadas(List<Driver> drivers) {
        List<Driver> ordenados = new ArrayList<>(drivers);

        for (int i = 0; i < ordenados.size() - 1; i++) {
            for (int j = 0; j < ordenados.size() - 1 - i; j++) {

                if (ordenados.get(j).getSancionesConfirmadas()
                        > ordenados.get(j + 1).getSancionesConfirmadas()) {

                    Driver temporal = ordenados.get(j);
                    ordenados.set(j, ordenados.get(j + 1));
                    ordenados.set(j + 1, temporal);
                }
            }
        }

        return ordenados;
    }
}