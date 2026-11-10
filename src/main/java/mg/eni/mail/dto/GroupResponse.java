package mg.eni.mail.dto;
import java.time.Instant;
import java.util.UUID;
public record GroupResponse(UUID id, String name, String description, String createdByEmail, Instant createdAt) {}
