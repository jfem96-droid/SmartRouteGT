package com.smartroutegt.backend.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.smartroutegt.backend.entity.AuditLog;
import com.smartroutegt.backend.entity.User;
import com.smartroutegt.backend.repository.*;

@RestController
@RequestMapping("/api/admin/permanent-delete")
public class PermanentDeleteController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogRepository auditLogRepository;
    private final BusRepository busRepository;
    private final StopRepository stopRepository;
    private final DriverRepository driverRepository;
    private final RouteRepository routeRepository;
    private final DriverRouteAssignmentRepository assignmentRepository;
    private final TripRepository tripRepository;
    private final ViolationRepository violationRepository;
    private final BusLocationRepository busLocationRepository;
    private final StopVisitRepository stopVisitRepository;
    private final RouteStopRepository routeStopRepository;

    public PermanentDeleteController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuditLogRepository auditLogRepository,
            BusRepository busRepository,
            StopRepository stopRepository,
            DriverRepository driverRepository,
            RouteRepository routeRepository,
            DriverRouteAssignmentRepository assignmentRepository,
            TripRepository tripRepository,
            ViolationRepository violationRepository,
            BusLocationRepository busLocationRepository,
            StopVisitRepository stopVisitRepository,
            RouteStopRepository routeStopRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogRepository = auditLogRepository;
        this.busRepository = busRepository;
        this.stopRepository = stopRepository;
        this.driverRepository = driverRepository;
        this.routeRepository = routeRepository;
        this.assignmentRepository = assignmentRepository;
        this.tripRepository = tripRepository;
        this.violationRepository = violationRepository;
        this.busLocationRepository = busLocationRepository;
        this.stopVisitRepository = stopVisitRepository;
        this.routeStopRepository = routeStopRepository;
    }

    /*
     * Diagnóstico previo.
     * No necesita contraseña porque no modifica información.
     */
    @GetMapping("/{type}/{id}/check")
    public ResponseEntity<?> check(
            @PathVariable String type,
            @PathVariable Long id,
            Authentication authentication) {

        User admin = getAdmin(authentication);

        if (admin == null) {
            return ResponseEntity.status(403)
                    .body("Solo un administrador activo puede realizar esta operación");
        }

        return switch (type.trim().toLowerCase()) {
            case "bus" -> checkBus(id);
            case "stop" -> checkStop(id);
            case "driver" -> checkDriver(id);
            case "route" -> checkRoute(id);
            case "user" -> checkUser(id, admin);
            default -> ResponseEntity.badRequest()
                    .body("Tipo de recurso no válido");
        };
    }

    @Transactional
    @DeleteMapping("/{type}/{id}")
    public ResponseEntity<?> delete(
            @PathVariable String type,
            @PathVariable Long id,
            @RequestBody DeleteRequest request,
            Authentication authentication) {

        if (request == null ||
            request.getPassword() == null ||
            request.getPassword().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("La contraseña del administrador es obligatoria");
        }

        User admin = getAdmin(authentication);

        if (admin == null) {
            return ResponseEntity.status(403)
                    .body("Solo un administrador activo puede realizar esta operación");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                admin.getPassword())) {

            return ResponseEntity.status(403)
                    .body("Contraseña de administrador incorrecta");
        }

        try {
            return switch (type.trim().toLowerCase()) {
                case "bus" -> deleteBus(id, admin.getEmail());
                case "stop" -> deleteStop(id, admin.getEmail());
                case "driver" -> deleteDriver(id, admin.getEmail());
                case "route" -> deleteRoute(id, admin.getEmail());
                case "user" -> deleteUser(id, admin);
                default -> ResponseEntity.badRequest()
                        .body("Tipo de recurso no válido");
            };

        } catch (DataIntegrityViolationException ex) {

            return ResponseEntity.status(409).body(
                    "La base de datos todavía contiene registros relacionados. " +
                    "Consulta nuevamente las dependencias antes de eliminar.");
        }
    }

    private User getAdmin(Authentication authentication) {

        if (authentication == null ||
            authentication.getName() == null) {
            return null;
        }

        User admin = userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElse(null);

        if (admin == null ||
            admin.getRole() != User.Role.ADMIN ||
            !admin.isActivo()) {
            return null;
        }

        return admin;
    }

    /* =====================================================
       DIAGNÓSTICOS
       ===================================================== */

    private ResponseEntity<?> checkBus(Long id) {

        var x = busRepository.findById(id).orElse(null);

        if (x == null)
            return ResponseEntity.notFound().build();

        List<Map<String,Object>> deps = new ArrayList<>();

        addDependency(
                deps,
                "Asignaciones",
                assignmentRepository.countByBusId(id),
                "DESASOCIAR",
                "Ve a Pilotos y elimina o desactiva las asignaciones de este bus.");

        addDependency(
                deps,
                "Viajes históricos",
                tripRepository.countByBusId(id),
                "HISTORIAL",
                historyMessage());

        addDependency(
                deps,
                "Sanciones",
                violationRepository.countByBusId(id),
                "HISTORIAL",
                historyMessage());

        addDependency(
                deps,
                "Ubicaciones GPS",
                busLocationRepository.countByBusId(id),
                "HISTORIAL",
                historyMessage());

        return dependencyResponse(
                "BUS",
                id,
                x.getCodigo(),
                deps);
    }

    private ResponseEntity<?> checkStop(Long id) {

        var x = stopRepository.findById(id).orElse(null);

        if (x == null)
            return ResponseEntity.notFound().build();

        List<Map<String,Object>> deps = new ArrayList<>();

        addDependency(
                deps,
                "Rutas asociadas",
                routeStopRepository.countByStopId(id),
                "DESASOCIAR",
                "Ve a Rutas → Paradas y quita esta parada de cada ruta.");

        addDependency(
                deps,
                "Visitas históricas",
                stopVisitRepository.countByStopId(id),
                "HISTORIAL",
                historyMessage());

        return dependencyResponse(
                "PARADA",
                id,
                x.getNombre(),
                deps);
    }

    private ResponseEntity<?> checkDriver(Long id) {

        var x = driverRepository.findById(id).orElse(null);

        if (x == null)
            return ResponseEntity.notFound().build();

        List<Map<String,Object>> deps = new ArrayList<>();

        addDependency(
                deps,
                "Usuario asociado",
                userRepository.existsByDriverId(id) ? 1 : 0,
                "DESASOCIAR",
                "Elimina primero el usuario asociado a este piloto.");

        addDependency(
                deps,
                "Asignaciones",
                assignmentRepository.countByDriverId(id),
                "DESASOCIAR",
                "Elimina o desactiva las asignaciones del piloto.");

        addDependency(
                deps,
                "Viajes históricos",
                tripRepository.countByDriverId(id),
                "HISTORIAL",
                historyMessage());

        addDependency(
                deps,
                "Sanciones",
                violationRepository.countByDriverId(id),
                "HISTORIAL",
                historyMessage());

        return dependencyResponse(
                "PILOTO",
                id,
                x.getNombre(),
                deps);
    }

    private ResponseEntity<?> checkRoute(Long id) {

        var x = routeRepository.findById(id).orElse(null);

        if (x == null)
            return ResponseEntity.notFound().build();

        List<Map<String,Object>> deps = new ArrayList<>();

        addDependency(
                deps,
                "Paradas asociadas",
                routeStopRepository.countByRouteId(id),
                "DESASOCIAR",
                "Ve a Rutas → Paradas y quita las paradas asociadas.");

        addDependency(
                deps,
                "Asignaciones",
                assignmentRepository.countByRouteId(id),
                "DESASOCIAR",
                "Elimina o desactiva las asignaciones relacionadas con esta ruta.");

        addDependency(
                deps,
                "Viajes históricos",
                tripRepository.countByRouteId(id),
                "HISTORIAL",
                historyMessage());

        return dependencyResponse(
                "RUTA",
                id,
                x.getNombre(),
                deps);
    }

    private ResponseEntity<?> checkUser(Long id, User admin) {

        User x = userRepository.findById(id).orElse(null);

        if (x == null)
            return ResponseEntity.notFound().build();

        List<Map<String,Object>> deps = new ArrayList<>();

        if (x.getId().equals(admin.getId())) {

            addDependency(
                    deps,
                    "Sesión administrativa actual",
                    1,
                    "PROTEGIDO",
                    "No puedes eliminar el usuario con el que tienes iniciada la sesión.");
        }

        return dependencyResponse(
                "USUARIO",
                id,
                x.getNombre(),
                deps);
    }

    private void addDependency(
            List<Map<String,Object>> deps,
            String label,
            long count,
            String type,
            String action) {

        if (count <= 0)
            return;

        Map<String,Object> d = new LinkedHashMap<>();

        d.put("label", label);
        d.put("count", count);
        d.put("type", type);
        d.put("action", action);

        deps.add(d);
    }

    private String historyMessage() {

        return "Este historial se conserva durante 7 días. " +
               "Cuando los registros hayan sido eliminados por la política de retención, " +
               "podrás intentar nuevamente la eliminación.";
    }

    private ResponseEntity<?> dependencyResponse(
            String entityType,
            Long id,
            String label,
            List<Map<String,Object>> deps) {

        Map<String,Object> result = new LinkedHashMap<>();

        result.put("entityType", entityType);
        result.put("entityId", id);
        result.put("label", label);
        result.put("canDelete", deps.isEmpty());
        result.put("retentionDays", 7);
        result.put("dependencies", deps);

        if (deps.isEmpty()) {
            result.put(
                    "message",
                    "No existen dependencias. Puede eliminarse permanentemente.");
        } else {
            result.put(
                    "message",
                    "Debes resolver las dependencias indicadas antes de eliminar.");
        }

        return ResponseEntity.ok(result);
    }

    /* =====================================================
       ELIMINACIÓN
       ===================================================== */

    private ResponseEntity<?> deleteBus(Long id,String actor) {

        var check = checkBus(id);

        if (!canDelete(check))
            return ResponseEntity.status(409).body(check.getBody());

        var x = busRepository.findById(id).orElse(null);

        if (x == null)
            return ResponseEntity.notFound().build();

        String label = x.getCodigo();

        busRepository.delete(x);
        busRepository.flush();

        audit(
                "ELIMINAR_BUS",
                actor,
                "Bus eliminado permanentemente: " + label,
                "BUS",
                id);

        return ResponseEntity.ok("Bus eliminado permanentemente");
    }

    private ResponseEntity<?> deleteStop(Long id,String actor) {

        var check = checkStop(id);

        if (!canDelete(check))
            return ResponseEntity.status(409).body(check.getBody());

        var x = stopRepository.findById(id).orElse(null);

        if (x == null)
            return ResponseEntity.notFound().build();

        String label = x.getNombre();

        stopRepository.delete(x);
        stopRepository.flush();

        audit(
                "ELIMINAR_PARADA",
                actor,
                "Parada eliminada permanentemente: " + label,
                "STOP",
                id);

        return ResponseEntity.ok("Parada eliminada permanentemente");
    }

    private ResponseEntity<?> deleteDriver(Long id,String actor) {

        var check = checkDriver(id);

        if (!canDelete(check))
            return ResponseEntity.status(409).body(check.getBody());

        var x = driverRepository.findById(id).orElse(null);

        if (x == null)
            return ResponseEntity.notFound().build();

        String label = x.getNombre();

        driverRepository.delete(x);
        driverRepository.flush();

        audit(
                "ELIMINAR_PILOTO",
                actor,
                "Piloto eliminado permanentemente: " + label,
                "DRIVER",
                id);

        return ResponseEntity.ok("Piloto eliminado permanentemente");
    }

    private ResponseEntity<?> deleteRoute(Long id,String actor) {

        var check = checkRoute(id);

        if (!canDelete(check))
            return ResponseEntity.status(409).body(check.getBody());

        var x = routeRepository.findById(id).orElse(null);

        if (x == null)
            return ResponseEntity.notFound().build();

        String label = x.getNombre();

        routeRepository.delete(x);
        routeRepository.flush();

        audit(
                "ELIMINAR_RUTA",
                actor,
                "Ruta eliminada permanentemente: " + label,
                "ROUTE",
                id);

        return ResponseEntity.ok("Ruta eliminada permanentemente");
    }

    private ResponseEntity<?> deleteUser(Long id,User admin) {

        User x = userRepository.findById(id).orElse(null);

        if (x == null)
            return ResponseEntity.notFound().build();

        if (x.getId().equals(admin.getId())) {
            return ResponseEntity.status(409).body(
                    "No puedes eliminar el usuario con el que tienes iniciada la sesión.");
        }

        String label = x.getEmail();

        userRepository.delete(x);
        userRepository.flush();

        audit(
                "ELIMINAR_USUARIO",
                admin.getEmail(),
                "Usuario eliminado permanentemente: " + label,
                "USER",
                id);

        return ResponseEntity.ok("Usuario eliminado permanentemente");
    }

    @SuppressWarnings("unchecked")
    private boolean canDelete(ResponseEntity<?> response) {

        if (!(response.getBody() instanceof Map))
            return false;

        Map<String,Object> body =
                (Map<String,Object>) response.getBody();

        return Boolean.TRUE.equals(body.get("canDelete"));
    }

    private void audit(
            String action,
            String actor,
            String description,
            String entity,
            Long id) {

        auditLogRepository.save(
                new AuditLog(
                        action,
                        actor,
                        description,
                        entity,
                        id));
    }

    public static class DeleteRequest {

        private String password;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
