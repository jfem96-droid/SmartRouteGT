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
import com.smartroutegt.backend.service.EmailVerificationService;
import com.smartroutegt.backend.service.EmailVerificationService.VerificationResult;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(
            UserRepository userRepository,
            DriverRepository driverRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailVerificationService = emailVerificationService;
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

        if (request.getTelefono() == null || request.getTelefono().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("El teléfono es obligatorio");
        }

        String telefono = request.getTelefono().trim();

        if (!telefono.matches("[0-9+()\\- ]{8,20}")) {
            return ResponseEntity.badRequest()
                    .body("El teléfono no tiene un formato válido");
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
        user.setTelefono(telefono);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActivo(true);
        user.setEmailVerified(false);
        user.setFechaCreacion(LocalDateTime.now());

        if (role == User.Role.DRIVER) {
            user.setDriver(driver);
        }

        User savedUser = userRepository.save(user);

        try {
            emailVerificationService.sendVerificationCode(savedUser);
        } catch (Exception exception) {
            userRepository.delete(savedUser);

            return ResponseEntity.internalServerError()
                    .body("No fue posible enviar el código de verificación. Intenta nuevamente.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("nombre", savedUser.getNombre());
        response.put("email", savedUser.getEmail());
        response.put("role", savedUser.getRole());
        response.put("activo", savedUser.isActivo());
        response.put("emailVerified", savedUser.isEmailVerified());
        response.put("verificationRequired", true);
        response.put("message", "Te enviamos un código de verificación a tu correo");
        response.put("fechaCreacion", savedUser.getFechaCreacion());

        if (savedUser.getDriver() != null) {
            response.put("driverId", savedUser.getDriver().getId());
            response.put("licencia", savedUser.getDriver().getLicencia());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(
            @RequestBody VerifyEmailRequest request) {

        if (request.getEmail() == null ||
                request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("El correo es obligatorio");
        }

        if (request.getCode() == null ||
                !request.getCode().matches("\\d{6}")) {
            return ResponseEntity.badRequest()
                    .body("El código debe contener 6 dígitos");
        }

        String email =
                request.getEmail().trim().toLowerCase();

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body("No existe una cuenta con este correo");
        }

        if (user.isEmailVerified()) {
            return ResponseEntity.ok(
                    Map.of(
                            "verified", true,
                            "message", "El correo ya se encuentra verificado"
                    )
            );
        }

        VerificationResult result =
                emailVerificationService.verify(
                        user,
                        request.getCode()
                );

        switch (result) {

            case SUCCESS:
                user.setEmailVerified(true);
                userRepository.save(user);

                return ResponseEntity.ok(
                        Map.of(
                                "verified", true,
                                "message", "Correo verificado correctamente"
                        )
                );

            case EXPIRED:
                return ResponseEntity.badRequest()
                        .body("El código ha vencido");

            case TOO_MANY_ATTEMPTS:
                return ResponseEntity.badRequest()
                        .body("Se alcanzó el máximo de intentos. Solicita un nuevo código");

            case INVALID:
                return ResponseEntity.badRequest()
                        .body("El código ingresado no es correcto");

            default:
                return ResponseEntity.badRequest()
                        .body("No existe un código de verificación válido");
        }
    }

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(
            @RequestBody ResendCodeRequest request) {

        if (request.getEmail() == null ||
                request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("El correo es obligatorio");
        }

        String email =
                request.getEmail().trim().toLowerCase();

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body("No existe una cuenta con este correo");
        }

        if (user.isEmailVerified()) {
            return ResponseEntity.badRequest()
                    .body("El correo ya se encuentra verificado");
        }

        try {
            emailVerificationService
                    .sendVerificationCode(user);

            return ResponseEntity.ok(
                    Map.of(
                            "sent", true,
                            "message", "Código enviado correctamente"
                    )
            );

        } catch (Exception exception) {
            return ResponseEntity.internalServerError()
                    .body("No fue posible enviar el código de verificación");
        }
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

        if (user.getRole() == User.Role.PASSENGER
                && !user.isEmailVerified()) {
            return ResponseEntity.status(403)
                    .body("Debes verificar tu correo antes de iniciar sesión");
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
        private String telefono;
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

        public String getTelefono() {
            return telefono;
        }

        public void setTelefono(String telefono) {
            this.telefono = telefono;
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

    public static class VerifyEmailRequest {

        private String email;
        private String code;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public static class ResendCodeRequest {

        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
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