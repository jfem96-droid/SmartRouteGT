package com.smartroutegt.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;

import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartroutegt.backend.entity.PasswordResetCode;
import com.smartroutegt.backend.entity.User;
import com.smartroutegt.backend.repository.PasswordResetCodeRepository;

@Service
public class PasswordResetService {

    private static final int MAX_ATTEMPTS = 5;

    private final PasswordResetCodeRepository repository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.verification.expiration-minutes:10}")
    private long expirationMinutes;

    public PasswordResetService(
            PasswordResetCodeRepository repository,
            JavaMailSender mailSender,
            PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    public void sendResetCode(User user) {

        repository
                .findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(user.getId())
                .ifPresent(previous -> {
                    previous.setUsed(true);
                    repository.save(previous);
                });

        String code = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        PasswordResetCode reset = new PasswordResetCode();

        reset.setUserId(user.getId());
        reset.setCodeHash(passwordEncoder.encode(code));
        reset.setCreatedAt(LocalDateTime.now());
        reset.setExpiresAt(
                LocalDateTime.now().plusMinutes(expirationMinutes)
        );
        reset.setUsed(false);
        reset.setAttempts(0);

        repository.save(reset);

        sendEmail(user, code);
    }

    private void sendEmail(User user, String code) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            if (from != null && !from.isBlank()) {
                helper.setFrom(from, "SmartRoute GT");
            }

            helper.setTo(user.getEmail());
            helper.setSubject(
                    "Recupera tu contraseña | SmartRoute GT"
            );

            String safeName = escapeHtml(user.getNombre());

            String html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <body style="margin:0;background:#f2f5f9;
                    font-family:Arial,Helvetica,sans-serif;">

                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="padding:30px 12px;background:#f2f5f9;">
                    <tr><td align="center">

                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="max-width:620px;background:#ffffff;
                           border-radius:18px;overflow:hidden;
                           border:1px solid #e5eaf0;">

                    <tr>
                    <td align="center"
                        style="background:#082b57;padding:28px;">

                    <img src="cid:smartrouteLogo"
                         width="160"
                         alt="SmartRoute GT"
                         style="display:block;margin:0 auto 14px;">

                    <div style="color:#ffffff;font-size:25px;
                                font-weight:bold;">
                        SmartRoute GT
                    </div>

                    </td>
                    </tr>

                    <tr>
                    <td style="padding:36px 38px;color:#4b5565;
                               font-size:16px;line-height:1.7;">

                    <div style="font-size:27px;font-weight:bold;
                                color:#082b57;margin-bottom:20px;">
                        Recupera tu contraseña
                    </div>

                    Hola <strong>%s</strong>,
                    <br><br>

                    Recibimos una solicitud para cambiar la contraseña
                    de tu cuenta de SmartRoute GT.

                    <br><br>

                    Tu código de recuperación es:

                    <div style="margin:26px 0;text-align:center;">
                        <span style="display:inline-block;
                                     background:#e8f6ff;
                                     border:1px solid #b8e2fb;
                                     border-radius:12px;
                                     padding:18px 28px;
                                     font-size:36px;
                                     font-weight:bold;
                                     letter-spacing:8px;
                                     color:#082b57;">
                            %s
                        </span>
                    </div>

                    Este código vence en
                    <strong>%d minutos</strong>.

                    <br><br>

                    Si no solicitaste este cambio, ignora este correo.
                    No compartas el código con nadie.

                    </td>
                    </tr>

                    </table>

                    </td></tr>
                    </table>

                    </body>
                    </html>
                    """.formatted(
                    safeName,
                    code,
                    expirationMinutes
            );

            helper.setText(html, true);

            ClassPathResource logo =
                    new ClassPathResource(
                            "static/images/smartroute-logo.png"
                    );

            helper.addInline(
                    "smartrouteLogo",
                    logo,
                    "image/png"
            );

            mailSender.send(message);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "No fue posible enviar el correo de recuperación",
                    exception
            );
        }
    }

    public ResetResult validateCode(User user, String code) {

        PasswordResetCode reset =
                repository
                        .findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(
                                user.getId()
                        )
                        .orElse(null);

        if (reset == null) {
            return ResetResult.NO_CODE;
        }

        if (reset.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResetResult.EXPIRED;
        }

        if (reset.getAttempts() >= MAX_ATTEMPTS) {
            return ResetResult.TOO_MANY_ATTEMPTS;
        }

        if (!passwordEncoder.matches(code, reset.getCodeHash())) {

            reset.setAttempts(reset.getAttempts() + 1);
            repository.save(reset);

            return ResetResult.INVALID;
        }

        return ResetResult.SUCCESS;
    }

    public void consumeCode(User user) {

        repository
                .findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(user.getId())
                .ifPresent(reset -> {
                    reset.setUsed(true);
                    repository.save(reset);
                });
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public enum ResetResult {
        SUCCESS,
        INVALID,
        EXPIRED,
        TOO_MANY_ATTEMPTS,
        NO_CODE
    }
}
