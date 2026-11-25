package mg.eni.mail.service;

import mg.eni.mail.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AccountEmailService {
    private final JavaMailSender mailSender;
    private final String from;
    private final String frontendUrl;

    public AccountEmailService(JavaMailSender mailSender, @Value("${app.mail.from:}") String from,
                               @Value("${app.frontend-url}") String frontendUrl) {
        this.mailSender = mailSender;
        this.from = from;
        this.frontendUrl = frontendUrl;
    }

    public void sendNewAccountCredentials(String recipient, String firstName, String temporaryPassword) {
        if (from == null || from.isBlank())
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                "L'envoi d'e-mail n'est pas configuré : définissez SMTP_USERNAME, SMTP_PASSWORD et SMTP_FROM");
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(recipient);
            message.setSubject("Vos identifiants ENI Mail");
            message.setText("Bonjour " + firstName + ",\n\nVotre compte ENI Mail vient d'être créé.\n\n"
                + "Adresse e-mail : " + recipient + "\nMot de passe temporaire : " + temporaryPassword
                + "\n\nConnectez-vous puis modifiez ce mot de passe depuis votre profil.\n\nENI Mail");
            mailSender.send(message);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                "Impossible d'envoyer les identifiants par e-mail. Le compte n'a pas été créé.");
        }
    }

    public void sendPasswordResetLink(String recipient, String firstName, String token) {
        ensureConfigured();
        String link = frontendUrl.replaceAll("/+$", "") + "/reset-password?token="
            + java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(recipient);
            message.setSubject("Réinitialisation de votre mot de passe ENI Mail");
            message.setText("Bonjour " + firstName + ",\n\nNous avons reçu une demande de réinitialisation de votre mot de passe ENI Mail.\n\n"
                + "Utilisez ce lien dans les 15 minutes :\n" + link
                + "\n\nSi vous n'êtes pas à l'origine de cette demande, ignorez cet e-mail.\n\nENI Mail");
            mailSender.send(message);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Impossible d'envoyer l'e-mail de réinitialisation.");
        }
    }

    private void ensureConfigured() {
        if (from == null || from.isBlank())
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                "L'envoi d'e-mail n'est pas configuré.");
    }
}
