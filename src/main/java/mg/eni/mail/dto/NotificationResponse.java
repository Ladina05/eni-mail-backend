package mg.eni.mail.dto;
import java.time.Instant;
import java.util.UUID;
public record NotificationResponse(UUID id, String title, String content, boolean read, Instant createdAt, UUID messageId) {}
