package mg.eni.mail.dto;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
public record MessageResponse(UUID id, String subject, String body, String senderEmail,
                              String senderName, Instant sentAt, boolean read,
                              boolean starred, boolean archived, boolean draft,
                              List<AttachmentResponse> attachments) {}
