package mg.eni.mail.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
public record SendMessageRequest(
    @NotBlank @Size(max=250) String subject,
    @NotBlank String body,
    List<String> to,
    List<String> cc,
    List<String> bcc
) {}
