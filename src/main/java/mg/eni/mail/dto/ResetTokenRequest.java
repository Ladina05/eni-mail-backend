package mg.eni.mail.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetTokenRequest(@NotBlank String token) {}
