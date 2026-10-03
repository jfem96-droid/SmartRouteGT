package com.smartroutegt.backend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.Driver;
import com.smartroutegt.backend.entity.User;
import com.smartroutegt.backend.repository.DriverRepository;
import com.smartroutegt.backend.repository.UserRepository;
import com.smartroutegt.backend.service.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UserRepository userRepository,
            DriverRepository driverRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre es obligatorio");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El correo es obligatorio");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            return ResponseEntity.badRequest()
                    .body("La contraseña debe tener al menos 8 caracteres");
        }

        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El rol es obligatorio");
        }

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.badRequest()
                    .body("Ya existe un usuario registrado con este correo");
        }

        User.Role role;

        try {
            role = User.Role.valueOf(request.getRole().trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body("El rol indicado no es válido");
        }

        if (role != User.Role.PASSENGER) {
            return ResponseEntity.badRequest()
                    .body("El registro público solo está disponible para pasajeros");
        }

        Driver driver = null;

        if (role == User.Role.DRIVER) {

            if (request.getLicencia() == null || request.getLicencia().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("La licencia es obligatoria para registrar un piloto");
            }

            driver = driverRepository.findByLicencia(request.getLicencia().trim());

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
        }

        User user = new User();
        user.setNombre(request.getNombre().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActivo(true);
        user.setFechaCreacion(LocalDateTime.now());

        if (role == User.Role.DRIVER) {
            user.setDriver(driver);
        }

        User savedUser = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("nombre", savedUser.getNombre());
        response.put("email", savedUser.getEmail());
        response.put("role", savedUser.getRole());
        response.put("activo", savedUser.isActivo());
        response.put("fechaCreacion", savedUser.getFechaCreacion());

        if (savedUser.getDriver() != null) {
            response.put("driverId", savedUser.getDriver().getId());
            response.put("licencia", savedUser.getDriver().getLicencia());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El correo es obligatorio");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("La contraseña es obligatoria");
        }

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401)
                    .body("Correo o contraseña incorrectos");
        }

        if (!user.isActivo()) {
            return ResponseEntity.status(403)
                    .body("El usuario se encuentra inactivo");
        }

        if (user.getRole() == User.Role.DRIVER) {
            if (user.getDriver() == null || !user.getDriver().isActivo()) {
                return ResponseEntity.status(403)
                        .body("El piloto se encuentra inactivo o no está vinculado");
            }
        }

        String token = jwtService.generateToken(user);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", 86400);
        response.put("id", user.getId());
        response.put("nombre", user.getNombre());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());

        if (user.getDriver() != null) {
            response.put("driverId", user.getDriver().getId());
            response.put("licencia", user.getDriver().getLicencia());
        }

        return ResponseEntity.ok(response);
    }

    public static class RegisterRequest {

        private String nombre;
        private String email;
        private String password;
        private String role;
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

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getLicencia() {
            return licencia;
        }

        public void setLicencia(String licencia) {
            this.licencia = licencia;
        }
    }

    public static class LoginRequest {

        private String email;
        private String password;

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
}