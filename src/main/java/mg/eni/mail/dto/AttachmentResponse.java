package mg.eni.mail.dto;
import java.time.Instant;
import java.util.UUID;
public record AttachmentResponse(UUID id, String originalName, String contentType, long size, Instant uploadedAt) {}
