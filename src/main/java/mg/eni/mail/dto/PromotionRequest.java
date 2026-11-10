package mg.eni.mail.dto;
import jakarta.validation.constraints.NotBlank;
public record PromotionRequest(@NotBlank String name, String description) {}
