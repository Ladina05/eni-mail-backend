package mg.eni.mail.dto;
import jakarta.validation.constraints.*;
import mg.eni.mail.entity.*;
import java.util.UUID;
public record UserCreateRequest(
    @NotBlank @Size(max=80) String firstName,
    @NotBlank @Size(max=80) String lastName,
    @NotBlank @Email String email,
    @NotNull Role role,
    UUID departmentId,
    UUID promotionId,
    StudyLevel studyLevel,
    AcademicMention academicMention,
    AcademicPath academicPath,
    TeacherPosition teacherPosition,
    @Size(max=160) String institutionalResponsibility,
    @Size(max=160) String staffPosition,
    @Size(max=40) String studentNumber
) {}
