package edu.uclm.reper.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import edu.uclm.reper.dto.EmailApiRequestDTO;
import edu.uclm.reper.model.Experiment;

@Service
public class ExperimentNotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${brevo.api.url:https://api.brevo.com/v3/smtp/email}")
    private String brevoApiUrl;

    @Value("${brevo.sender.email:qsauron.noreply@gmail.com}")
    private String senderEmail;

    @Value("${brevo.sender.name:QSauron}")
    private String senderName;

    @Value("${app.frontend.url:https://alarcosj.esi.uclm.es/qumu}")
    private String frontendUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendExperimentCompletedEmail(Experiment experiment, String userEmail) {
        long seconds = experiment.getDurationSeconds() != null ? experiment.getDurationSeconds() : 0;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        String durationFormatted = String.format("%02d:%02d:%02d", hours, minutes, secs);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String finishedAtStr = experiment.getFinishedAt() != null ? sdf.format(experiment.getFinishedAt())
                : sdf.format(new Date());

        String subject = "QUMU - Experimento finalizado: " + experiment.getName();
        String htmlBody;
        try {
            htmlBody = loadEmailTemplate("email-templates/experiment-completed.html");
            htmlBody = htmlBody.replace("{{EXPERIMENT_NAME}}", escapeHtml(experiment.getName()))
                    .replace("{{COMPLETED_CIRCUITS}}",
                            String.valueOf(
                                    experiment.getCompletedCircuits() != null ? experiment.getCompletedCircuits() : 0))
                    .replace("{{TOTAL_CIRCUITS}}",
                            String.valueOf(experiment.getTotalCircuits() != null ? experiment.getTotalCircuits() : 0))
                    .replace("{{DURATION_FORMATTED}}", durationFormatted)
                    .replace("{{STATUS}}", experiment.getStatus() != null ? experiment.getStatus() : "COMPLETED")
                    .replace("{{FINISHED_AT}}", finishedAtStr)
                    .replace("{{FRONTEND_URL}}", frontendUrl);
        } catch (Exception e) {
            System.err.println("Could not load HTML template, fallback to inline HTML: " + e.getMessage());
            htmlBody = buildFallbackCompletedHtml(experiment, durationFormatted, finishedAtStr);
        }

        String textBody = "Hola,\n\n"
                + "El experimento '" + experiment.getName() + "' ha finalizado correctamente.\n\n"
                + "Resumen de ejecucion:\n"
                + "- Estado: " + experiment.getStatus() + "\n"
                + "- Circuitos procesados: " + experiment.getCompletedCircuits() + " / "
                + experiment.getTotalCircuits() + "\n"
                + "- Tiempo total de ejecucion: " + durationFormatted + " (" + seconds + " segundos)\n\n"
                + "Puedes consultar los resultados accediendo a la aplicacion:\n"
                + frontendUrl + "\n\n"
                + "Atentamente,\nEl equipo de QUMU";

        sendEmail(userEmail, subject, htmlBody, textBody);
    }

    public void sendExperimentFailedEmail(Experiment experiment, String userEmail, String errorMessage) {
        long seconds = experiment.getDurationSeconds() != null ? experiment.getDurationSeconds() : 0;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        String durationFormatted = String.format("%02d:%02d:%02d", hours, minutes, secs);

        String subject = "QUMU - Error en experimento: " + experiment.getName();

        String htmlBody;
        try {
            htmlBody = loadEmailTemplate("email-templates/experiment-failed.html");
            htmlBody = htmlBody.replace("{{EXPERIMENT_NAME}}", escapeHtml(experiment.getName()))
                    .replace("{{COMPLETED_CIRCUITS}}",
                            String.valueOf(
                                    experiment.getCompletedCircuits() != null ? experiment.getCompletedCircuits() : 0))
                    .replace("{{TOTAL_CIRCUITS}}",
                            String.valueOf(experiment.getTotalCircuits() != null ? experiment.getTotalCircuits() : 0))
                    .replace("{{DURATION_FORMATTED}}", durationFormatted)
                    .replace("{{FRONTEND_URL}}", frontendUrl);
        } catch (Exception e) {
            htmlBody = buildFallbackFailedHtml(experiment, durationFormatted);
        }

        String textBody = "Hola,\n\n"
                + "La ejecucion del experimento '" + experiment.getName() + "' se ha detenido debido a una incidencia.\n\n"
                + "Detalles:\n"
                + "- Estado: FAILED\n"
                + "- Circuitos completados antes del fallo: " + experiment.getCompletedCircuits() + " / "
                + experiment.getTotalCircuits() + "\n"
                + "- Tiempo transcurrido: " + seconds + " segundos\n\n"
                + "Puedes revisar el estado del proyecto en la aplicacion:\n"
                + frontendUrl + "\n\n"
                + "Atentamente,\nEl equipo de QUMU";

        sendEmail(userEmail, subject, htmlBody, textBody);
    }

    private String loadEmailTemplate(String path) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + path);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String escapeHtml(String input) {
        if (input == null)
            return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String buildFallbackCompletedHtml(Experiment experiment, String durationFormatted, String finishedAtStr) {
        return "<div style=\"font-family: Arial, sans-serif; background-color: #020617; color: #e2e8f0; padding: 30px; border-radius: 12px; max-width: 600px; margin: 0 auto; border: 1px solid rgba(16,185,129,0.3);\">"
                + "<h2 style=\"color: #10b981;\">Experimento Finalizado: " + escapeHtml(experiment.getName()) + "</h2>"
                + "<p><strong>Circuitos procesados:</strong> " + experiment.getCompletedCircuits() + " / "
                + experiment.getTotalCircuits() + "</p>"
                + "<p><strong>Tiempo total:</strong> " + durationFormatted + "</p>"
                + "<p><strong>Fecha:</strong> " + finishedAtStr + "</p>"
                + "<p><a href=\"" + frontendUrl
                + "\" style=\"background-color: #10b981; color: #fff; padding: 10px 20px; text-decoration: none; border-radius: 8px; display: inline-block;\">Ver Resultados</a></p>"
                + "</div>";
    }

    private String buildFallbackFailedHtml(Experiment experiment, String durationFormatted) {
        return "<div style=\"font-family: Arial, sans-serif; background-color: #020617; color: #e2e8f0; padding: 30px; border-radius: 12px; max-width: 600px; margin: 0 auto; border: 1px solid rgba(239,68,68,0.3);\">"
                + "<h2 style=\"color: #ef4444;\">Incidencia en Experimento: " + escapeHtml(experiment.getName()) + "</h2>"
                + "<p>El procesamiento no pudo completarse correctamente.</p>"
                + "<p><strong>Circuitos completados antes del fallo:</strong> " + experiment.getCompletedCircuits() + " / "
                + experiment.getTotalCircuits() + "</p>"
                + "<p><strong>Tiempo transcurrido:</strong> " + durationFormatted + "</p>"
                + "<p><a href=\"" + frontendUrl
                + "\" style=\"background-color: #ef4444; color: #fff; padding: 10px 20px; text-decoration: none; border-radius: 8px; display: inline-block;\">Revisar en QUMU</a></p>"
                + "</div>";
    }

    private void sendEmail(String to, String subject, String htmlContent, String plainTextFallback) {
        if (to == null || to.trim().isEmpty()) {
            return;
        }

        // 1. Try Brevo HTTP API first (preferred, same as Alaride)
        if (brevoApiKey != null && !brevoApiKey.trim().isEmpty()) {
            try {
                EmailApiRequestDTO requestBody = new EmailApiRequestDTO(
                        senderName, senderEmail, to, subject, htmlContent);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey);

                HttpEntity<EmailApiRequestDTO> requestEntity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(brevoApiUrl, requestEntity, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    return;
                } else {
                    System.err.println("Brevo API returned status: " + response.getStatusCode());
                }
            } catch (Exception e) {
                System.err.println("Failed to send email via Brevo API: " + e.getMessage());
            }
        }

        // 2. Fallback to JavaMailSender (SMTP) if available
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(senderEmail);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(plainTextFallback);
                mailSender.send(message);
                return;
            } catch (Exception e) {
                System.err.println("Failed to send email via SMTP (" + e.getMessage() + ")");
            }
        }
    }
}

