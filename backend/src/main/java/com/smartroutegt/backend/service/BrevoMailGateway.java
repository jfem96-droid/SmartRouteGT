package com.smartroutegt.backend.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Canal alternativo HTTPS para correos transaccionales.
 * La clave permanece únicamente en el backend mediante BREVO_API_KEY.
 */
@Service
public class BrevoMailGateway {
    private static final URI ENDPOINT = URI.create("https://api.brevo.com/v3/smtp/email");
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    @Value("${app.mail.provider:smtp}")
    private String provider;

    @Value("${app.mail.brevo.api-key:}")
    private String apiKey;

    @Value("${app.mail.brevo.sender:}")
    private String senderEmail;

    public boolean enabled() {
        return "brevo".equalsIgnoreCase(provider == null ? "" : provider.trim());
    }

    public void send(String recipient, String subject, String html) {
        if (!enabled()) {
            throw new IllegalStateException("El canal HTTPS de correo no está habilitado");
        }
        if (apiKey == null || apiKey.isBlank() || senderEmail == null || senderEmail.isBlank()) {
            throw new IllegalStateException("Falta configurar BREVO_API_KEY o BREVO_FROM_EMAIL en el servidor");
        }
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Destinatario de correo vacío");
        }
        // Brevo no utiliza referencias MIME cid; conservamos el encabezado textual de la plantilla.
        String htmlSinCid = html.replaceAll("(?is)<img\\s+[^>]*src\\s*=\\s*[\"']cid:smartrouteLogo[\"'][^>]*>", "");
        String body = "{\"sender\":{\"name\":\"SmartRoute GT\",\"email\":" + quote(senderEmail.trim())
                + "},\"to\":[{\"email\":" + quote(recipient.trim())
                + "}],\"subject\":" + quote(subject)
                + ",\"htmlContent\":" + quote(htmlSinCid) + "}";
        HttpRequest request = HttpRequest.newBuilder(ENDPOINT)
                .timeout(Duration.ofSeconds(8))
                .header("api-key", apiKey.trim())
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                // No registrar la clave ni el contenido del correo, que incluye el código OTP.
                throw new IllegalStateException("Brevo rechazó el envío HTTP " + status
                        + ". Comprueba clave API y remitente verificado.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpió la comunicación HTTPS con Brevo", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo contactar el proveedor de correo HTTPS", exception);
        }
    }

    private static String quote(String text) {
        StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\b' -> b.append("\\b");
                case '\f' -> b.append("\\f");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 0x20) {
                        b.append(String.format(Locale.ROOT, "\\u%04x", (int)c));
                    } else {
                        b.append(c);
                    }
                }
            }
        }
        return b.append('"').toString();
    }
}
