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

import com.smartroutegt.backend.entity.EmailVerificationCode;
import com.smartroutegt.backend.entity.User;
import com.smartroutegt.backend.repository.EmailVerificationCodeRepository;

@Service
public class EmailVerificationService {

    private static final int MAX_ATTEMPTS = 5;

    private final EmailVerificationCodeRepository codeRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.verification.expiration-minutes:10}")
    private long expirationMinutes;

    public EmailVerificationService(
            EmailVerificationCodeRepository codeRepository,
            JavaMailSender mailSender,
            PasswordEncoder passwordEncoder) {

        this.codeRepository = codeRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    public void sendVerificationCode(User user) {

        invalidatePreviousCode(user.getId());

        String code = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        EmailVerificationCode verification =
                new EmailVerificationCode();

        verification.setUserId(user.getId());
        verification.setCodeHash(
                passwordEncoder.encode(code)
        );
        verification.setCreatedAt(LocalDateTime.now());
        verification.setExpiresAt(
                LocalDateTime.now().plusMinutes(expirationMinutes)
        );
        verification.setUsed(false);
        verification.setAttempts(0);

        codeRepository.save(verification);

        sendHtmlEmail(user, code);
    }

    private void sendHtmlEmail(User user, String code) {

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
                    "Tu código de verificación | SmartRoute GT"
            );

            String safeName = escapeHtml(user.getNombre());

            String html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport"
                              content="width=device-width, initial-scale=1.0">
                    </head>

                    <body style="
                        margin:0;
                        padding:0;
                        background-color:#f2f5f9;
                        font-family:Arial,Helvetica,sans-serif;
                        color:#172033;
                    ">

                    <table role="presentation"
                           width="100%%"
                           cellspacing="0"
                           cellpadding="0"
                           border="0"
                           style="background-color:#f2f5f9;
                                  padding:30px 12px;">

                        <tr>
                            <td align="center">

                                <table role="presentation"
                                       width="100%%"
                                       cellspacing="0"
                                       cellpadding="0"
                                       border="0"
                                       style="
                                           max-width:620px;
                                           background-color:#ffffff;
                                           border-radius:18px;
                                           overflow:hidden;
                                           border:1px solid #e5eaf0;
                                       ">

                                    <tr>
                                        <td align="center"
                                            bgcolor="#082b57"
                                            style="
                                                background-color:#082b57;
                                                padding:30px 24px 26px;
                                            ">

                                            <img
                                                src="cid:smartrouteLogo"
                                                alt="SmartRoute GT"
                                                width="170"
                                                style="
                                                    display:block;
                                                    width:170px;
                                                    max-width:170px;
                                                    height:auto;
                                                    margin:0 auto 15px;
                                                    border:0;
                                                "
                                            >

                                            <div style="
                                                color:#ffffff;
                                                font-size:26px;
                                                font-weight:bold;
                                                line-height:1.2;
                                            ">
                                                SmartRoute GT
                                            </div>

                                            <div style="
                                                color:#61c7ff;
                                                font-size:11px;
                                                letter-spacing:2px;
                                                margin-top:8px;
                                                text-transform:uppercase;
                                            ">
                                                Transporte más seguro para todos
                                            </div>

                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="
                                            padding:38px 38px 12px;
                                        ">

                                            <div style="
                                                font-size:28px;
                                                line-height:1.25;
                                                font-weight:bold;
                                                color:#082b57;
                                                margin-bottom:20px;
                                            ">
                                                Código de verificación
                                            </div>

                                            <div style="
                                                font-size:16px;
                                                line-height:1.7;
                                                color:#4b5565;
                                            ">
                                                Hola <strong>%s</strong>,
                                                <br><br>

                                                Gracias por registrarte en
                                                <strong style="color:#082b57;">
                                                    SmartRoute GT
                                                </strong>.

                                                Para completar tu registro y
                                                activar tu cuenta, ingresa el
                                                siguiente código de verificación:
                                            </div>

                                        </td>
                                    </tr>

                                    <tr>
                                        <td align="center"
                                            style="padding:20px 25px 14px;">

                                            <table role="presentation"
                                                   cellspacing="0"
                                                   cellpadding="0"
                                                   border="0">

                                                <tr>
                                                    <td align="center"
                                                        bgcolor="#e8f6ff"
                                                        style="
                                                            background-color:#e8f6ff;
                                                            border:1px solid #b8e2fb;
                                                            border-radius:12px;
                                                            padding:18px 28px;
                                                            font-size:36px;
                                                            line-height:1;
                                                            font-weight:bold;
                                                            letter-spacing:8px;
                                                            color:#082b57;
                                                        ">
                                                        %s
                                                    </td>
                                                </tr>

                                            </table>

                                        </td>
                                    </tr>

                                    <tr>
                                        <td align="center"
                                            style="
                                                padding:10px 38px 30px;
                                                color:#647184;
                                                font-size:14px;
                                                line-height:1.7;
                                            ">

                                            Este código es válido durante
                                            <strong>%d minutos</strong>.

                                            <br>

                                            Por tu seguridad, no compartas
                                            este código con nadie.

                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:0 38px;">
                                            <div style="
                                                border-top:1px solid #e5eaf0;
                                            "></div>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="
                                            padding:25px 38px 32px;
                                            color:#7a8797;
                                            font-size:13px;
                                            line-height:1.65;
                                        ">

                                            Si no solicitaste esta cuenta,
                                            puedes ignorar este correo de
                                            forma segura.

                                            <br><br>

                                            <strong style="
                                                color:#082b57;
                                                font-size:15px;
                                            ">
                                                SmartRoute GT
                                            </strong>

                                            <br>

                                            Transporte más seguro ·
                                            Comunidad conectada ·
                                            Ciudades sostenibles

                                        </td>
                                    </tr>

                                </table>

                                <div style="
                                    max-width:620px;
                                    padding:18px 20px;
                                    color:#9aa5b3;
                                    font-size:11px;
                                    line-height:1.5;
                                    text-align:center;
                                ">
                                    Este es un mensaje automático de
                                    SmartRoute GT.
                                    Por favor, no respondas a este correo.
                                </div>

                            </td>
                        </tr>

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
                    "No fue posible enviar el correo de verificación",
                    exception
            );
        }
    }

    public VerificationResult verify(
            User user,
            String code) {

        EmailVerificationCode verification =
                codeRepository
                        .findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(
                                user.getId()
                        )
                        .orElse(null);

        if (verification == null) {
            return VerificationResult.NO_CODE;
        }

        if (verification.isUsed()) {
            return VerificationResult.ALREADY_USED;
        }

        if (verification.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            return VerificationResult.EXPIRED;
        }

        if (verification.getAttempts() >= MAX_ATTEMPTS) {
            return VerificationResult.TOO_MANY_ATTEMPTS;
        }

        if (!passwordEncoder.matches(
                code,
                verification.getCodeHash())) {

            verification.setAttempts(
                    verification.getAttempts() + 1
            );

            codeRepository.save(verification);

            return VerificationResult.INVALID;
        }

        verification.setUsed(true);
        codeRepository.save(verification);

        return VerificationResult.SUCCESS;
    }

    private void invalidatePreviousCode(Long userId) {

        codeRepository
                .findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(userId)
                .ifPresent(previous -> {
                    previous.setUsed(true);
                    codeRepository.save(previous);
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

    public enum VerificationResult {
        SUCCESS,
        INVALID,
        EXPIRED,
        TOO_MANY_ATTEMPTS,
        ALREADY_USED,
        NO_CODE
    }
}
