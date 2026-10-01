package com.smartroutegt.backend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.Driver;
import com.smartroutegt.backend.entity.User;
import com.smartroutegt.backend.repository.DriverRepository;
import com.smartroutegt.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserRepository userRepository,
            DriverRepository driverRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
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

    @PostMapping("/drivers")
    public ResponseEntity<?> createDriverUser(@RequestBody CreateDriverUserRequest request) {

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

        if (request.getLicencia() == null || request.getLicencia().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("La licencia es obligatoria");
        }

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.badRequest()
                    .body("Ya existe un usuario registrado con este correo");
        }

        Driver driver = driverRepository.findByLicencia(request.getLicencia().trim());

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

        return ResponseEntity.ok(buildUserResponse(savedUser));
    }

    private Map<String, Object> buildUserResponse(User user) {

        Map<String, Object> response = new HashMap<>();

        response.put("id", user.getId());
        response.put("nombre", user.getNombre());
        response.put("email", user.getEmail());
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
}