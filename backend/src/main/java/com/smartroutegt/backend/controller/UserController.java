package com.smartroutegt.backend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.AuditLog;
import com.smartroutegt.backend.entity.Driver;
import com.smartroutegt.backend.entity.User;
import com.smartroutegt.backend.repository.AuditLogRepository;
import com.smartroutegt.backend.repository.DriverRepository;
import com.smartroutegt.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserRepository userRepository,
            DriverRepository driverRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<?> getUsers() {

        List<Map<String, Object>> response = userRepository.findAll()
                .stream()
                .map(this::buildUserResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(buildUserResponse(user));
    }

    @Transactional
    @PostMapping("/drivers")
    public ResponseEntity<?> createDriverUser(
            @RequestBody CreateDriverUserRequest request) {

        String validationError = validateBasicUserData(
                request.getNombre(),
                request.getEmail(),
                request.getPassword());

        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        if (request.getLicencia() == null
                || request.getLicencia().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("La licencia es obligatoria");
        }

        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.badRequest()
                    .body("Ya existe un usuario registrado con este correo");
        }

        Driver driver = driverRepository.findByLicencia(
                request.getLicencia().trim());

        if (driver == null) {
            return ResponseEntity.badRequest()
                    .body("No existe un piloto registrado con esa licencia");
        }

        if (!driver.isActivo()) {
            return ResponseEntity.badRequest()
                    .body("El piloto se encuentra inactivo");
        }

        if (userRepository.existsByDriverId(driver.getId())) {
            return ResponseEntity.badRequest()
                    .body("Este piloto ya tiene una cuenta de usuario");
        }

        User user = new User();
        user.setNombre(request.getNombre().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.DRIVER);
        user.setActivo(true);
        user.setFechaCreacion(LocalDateTime.now());
        user.setDriver(driver);

        User savedUser = userRepository.save(user);

        registrarAuditoria(
                "CREAR_USUARIO",
                "Se creó una cuenta DRIVER para el piloto "
                        + driver.getNombre(),
                savedUser.getId());

        return ResponseEntity.ok(buildUserResponse(savedUser));
    }

    @Transactional
    @PostMapping("/admins")
    public ResponseEntity<?> createAdminUser(
            @RequestBody CreateAdminUserRequest request) {

        String validationError = validateBasicUserData(
                request.getNombre(),
                request.getEmail(),
                request.getPassword());

        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.badRequest()
                    .body("Ya existe un usuario registrado con este correo");
        }

        User user = new User();
        user.setNombre(request.getNombre().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.ADMIN);
        user.setActivo(true);
        user.setFechaCreacion(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        registrarAuditoria(
                "CREAR_USUARIO",
                "Se creó una cuenta ADMIN",
                savedUser.getId());

        return ResponseEntity.ok(buildUserResponse(savedUser));
    }

    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.getNombre() == null
                || request.getNombre().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("El nombre es obligatorio");
        }

        if (request.getEmail() == null
                || request.getEmail().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("El correo es obligatorio");
        }

        String email = normalizeEmail(request.getEmail());

        User existingUser = userRepository
                .findByEmailIgnoreCase(email)
                .orElse(null);

        if (existingUser != null
                && !existingUser.getId().equals(user.getId())) {

            return ResponseEntity.badRequest()
                    .body("Ya existe un usuario registrado con este correo");
        }

        user.setNombre(request.getNombre().trim());
        user.setEmail(email);

        if (request.getTelefono() != null) {
            String telefono = request.getTelefono().trim();

            if (!telefono.isEmpty()
                    && !telefono.matches("[0-9+()\\- ]{8,20}")) {

                return ResponseEntity.badRequest()
                        .body("El teléfono no tiene un formato válido");
            }

            user.setTelefono(
                    telefono.isEmpty() ? null : telefono);
        }

        User savedUser = userRepository.save(user);

        registrarAuditoria(
                "MODIFICAR_USUARIO",
                "Se modificaron el nombre y/o correo del usuario",
                savedUser.getId());

        return ResponseEntity.ok(buildUserResponse(savedUser));
    }

    @Transactional
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.getPassword() == null
                || request.getPassword().length() < 8) {

            return ResponseEntity.badRequest()
                    .body("La contraseña debe tener al menos 8 caracteres");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        registrarAuditoria(
                "CAMBIAR_PASSWORD_USUARIO",
                "Se cambió la contraseña del usuario",
                user.getId());

        return ResponseEntity.ok(
                Map.of(
                        "message", "Contraseña actualizada correctamente",
                        "userId", user.getId()));
    }

    @Transactional
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (!user.isActivo()) {
            return ResponseEntity.badRequest()
                    .body("El usuario ya se encuentra inactivo");
        }

        String authenticatedEmail = getAuthenticatedActor();

        if (user.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            return ResponseEntity.badRequest()
                    .body("No puede desactivar su propia cuenta");
        }

        if (user.getRole() == User.Role.ADMIN
                && countActiveAdmins() <= 1) {

            return ResponseEntity.badRequest()
                    .body("No se puede desactivar el último administrador activo");
        }

        user.setActivo(false);

        User savedUser = userRepository.save(user);

        registrarAuditoria(
                "DESACTIVAR_USUARIO",
                "Se desactivó la cuenta del usuario",
                savedUser.getId());

        return ResponseEntity.ok(buildUserResponse(savedUser));
    }

    @Transactional
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (user.isActivo()) {
            return ResponseEntity.badRequest()
                    .body("El usuario ya se encuentra activo");
        }

        if (user.getRole() == User.Role.DRIVER) {

            if (user.getDriver() == null) {
                return ResponseEntity.badRequest()
                        .body("La cuenta DRIVER no tiene un piloto vinculado");
            }

            if (!user.getDriver().isActivo()) {
                return ResponseEntity.badRequest()
                        .body("No se puede activar la cuenta porque el piloto se encuentra inactivo");
            }
        }

        user.setActivo(true);

        User savedUser = userRepository.save(user);

        registrarAuditoria(
                "ACTIVAR_USUARIO",
                "Se activó la cuenta del usuario",
                savedUser.getId());

        return ResponseEntity.ok(buildUserResponse(savedUser));
    }

    private String validateBasicUserData(
            String nombre,
            String email,
            String password) {

        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre es obligatorio";
        }

        if (email == null || email.trim().isEmpty()) {
            return "El correo es obligatorio";
        }

        if (!isValidEmail(email)) {
            return "El correo no tiene un formato válido";
        }

        if (password == null || password.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres";
        }

        return null;
    }

    private boolean isValidEmail(String email) {

        String normalized = email.trim();

        int atIndex = normalized.indexOf('@');
        int lastAtIndex = normalized.lastIndexOf('@');

        if (atIndex <= 0 || atIndex != lastAtIndex) {
            return false;
        }

        int dotIndex = normalized.indexOf('.', atIndex + 2);

        return dotIndex > atIndex + 1
                && dotIndex < normalized.length() - 1;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private long countActiveAdmins() {

        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == User.Role.ADMIN)
                .filter(User::isActivo)
                .count();
    }

    private String getAuthenticatedActor() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {

            return "SISTEMA";
        }

        return authentication.getName();
    }

    private void registrarAuditoria(
            String accion,
            String descripcion,
            Long userId) {

        AuditLog auditLog = new AuditLog(
                accion,
                getAuthenticatedActor(),
                descripcion,
                "USER",
                userId);

        auditLogRepository.save(auditLog);
    }

    private Map<String, Object> buildUserResponse(User user) {

        Map<String, Object> response = new HashMap<>();

        response.put("id", user.getId());
        response.put("nombre", user.getNombre());
        response.put("email", user.getEmail());
        response.put("telefono", user.getTelefono());
        response.put("role", user.getRole());
        response.put("activo", user.isActivo());
        response.put("fechaCreacion", user.getFechaCreacion());

        if (user.getDriver() != null) {
            response.put("driverId", user.getDriver().getId());
            response.put("licencia", user.getDriver().getLicencia());
        }

        return response;
    }

    public static class CreateDriverUserRequest {

        private String nombre;
        private String email;
        private String password;
        private String licencia;

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getLicencia() {
            return licencia;
        }

        public void setLicencia(String licencia) {
            this.licencia = licencia;
        }
    }

    public static class CreateAdminUserRequest {

        private String nombre;
        private String email;
        private String password;

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class UpdateUserRequest {

        private String nombre;
        private String email;
        private String telefono;

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getTelefono() {
            return telefono;
        }

        public void setTelefono(String telefono) {
            this.telefono = telefono;
        }
    }

    public static class ChangePasswordRequest {

        private String password;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}