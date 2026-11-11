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

    public AccountEmailService(JavaMailSender mailSender, @Value("${app.mail.from:}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendNewAccountCredentials(String recipient, String firstName, String temporaryPassword) {
        if (from == null || from.isBlank())
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                "L'envoi d'e-mail n'est pas configurÃ© : dÃ©finissez SMTP_USERNAME, SMTP_PASSWORD et SMTP_FROM");
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(recipient);
            message.setSubject("Vos identifiants ENI Mail");
            message.setText("Bonjour " + firstName + ",\n\nVotre compte ENI Mail vient d'Ãªtre crÃ©Ã©.\n\n"
                + "Adresse e-mail : " + recipient + "\nMot de passe temporaire : " + temporaryPassword
                + "\n\nConnectez-vous puis modifiez ce mot de passe depuis votre profil.\n\nENI Mail");
            mailSender.send(message);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                "Impossible d'envoyer les identifiants par e-mail. Le compte n'a pas Ã©tÃ© crÃ©Ã©.");
        }
    }
}
