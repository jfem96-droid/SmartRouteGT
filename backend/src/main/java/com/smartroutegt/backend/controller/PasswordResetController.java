package com.smartroutegt.backend.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.User;
import com.smartroutegt.backend.repository.UserRepository;
import com.smartroutegt.backend.service.PasswordResetService;
import com.smartroutegt.backend.service.PasswordResetService.ResetResult;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordResetService passwordResetService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(
            UserRepository userRepository,
            PasswordResetService passwordResetService,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordResetService = passwordResetService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        String email = normalizeEmail(request.getEmail());

        if (email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("El correo electrónico es obligatorio");
        }

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElse(null);

        /*
         * No revelamos si un correo existe o no.
         * Esto evita enumeración de usuarios.
         */
        if (user == null || !user.isActivo()) {
            return ResponseEntity.ok(genericSentResponse());
        }

        try {
            passwordResetService.sendResetCode(user);
        } catch (Exception exception) {
            return ResponseEntity.internalServerError()
                    .body("No fue posible enviar el código de recuperación");
        }

        return ResponseEntity.ok(genericSentResponse());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        String email = normalizeEmail(request.getEmail());
        String code = request.getCode() == null
                ? ""
                : request.getCode().trim();

        String newPassword = request.getNewPassword() == null
                ? ""
                : request.getNewPassword();

        if (email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("El correo electrónico es obligatorio");
        }

        if (!code.matches("\\d{6}")) {
            return ResponseEntity.badRequest()
                    .body("El código debe contener 6 dígitos");
        }

        if (newPassword.length() < 8) {
            return ResponseEntity.badRequest()
                    .body("La nueva contraseña debe tener al menos 8 caracteres");
        }

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElse(null);

        if (user == null || !user.isActivo()) {
            return ResponseEntity.badRequest()
                    .body("Código de recuperación inválido");
        }

        ResetResult result =
                passwordResetService.validateCode(user, code);

        switch (result) {

            case INVALID:
                return ResponseEntity.badRequest()
                        .body("Código de recuperación incorrecto");

            case EXPIRED:
                return ResponseEntity.badRequest()
                        .body("El código de recuperación ha vencido");

            case TOO_MANY_ATTEMPTS:
                return ResponseEntity.badRequest()
                        .body("Demasiados intentos. Solicita un nuevo código");

            case NO_CODE:
                return ResponseEntity.badRequest()
                        .body("No existe un código de recuperación activo");

            case SUCCESS:
                break;
        }

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);
        passwordResetService.consumeCode(user);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("changed", true);
        response.put(
                "message",
                "Contraseña actualizada correctamente"
        );

        return ResponseEntity.ok(response);
    }

    private String normalizeEmail(String email) {
        return email == null
                ? ""
                : email.trim().toLowerCase();
    }

    private Map<String, Object> genericSentResponse() {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("sent", true);
        response.put(
                "message",
                "Si existe una cuenta asociada a ese correo, recibirás un código de recuperación"
        );

        return response;
    }

    public static class ForgotPasswordRequest {

        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class ResetPasswordRequest {

        private String email;
        private String code;
        private String newPassword;

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

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
