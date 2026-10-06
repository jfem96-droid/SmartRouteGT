package com.smartroutegt.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.service.PassengerEtaService;

@RestController
@RequestMapping("/api/trips")
public class PassengerEtaController {

    private final PassengerEtaService passengerEtaService;

    public PassengerEtaController(
            PassengerEtaService passengerEtaService) {

        this.passengerEtaService = passengerEtaService;
    }

    @GetMapping("/{tripId}/eta-to-stop")
    public ResponseEntity<?> getEtaToStop(
            @PathVariable Long tripId,
            @RequestParam Long stopId) {

        try {
            Map<String, Object> response =
                    passengerEtaService.calculate(
                            tripId,
                            stopId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException exception) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    exception.getMessage()
                            )
                    );

        } catch (Exception exception) {

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "error",
                                    "No fue posible calcular el ETA",
                                    "detalle",
                                    exception.getMessage() == null
                                            ? exception.getClass().getSimpleName()
                                            : exception.getMessage()
                            )
                    );
        }
    }
}
