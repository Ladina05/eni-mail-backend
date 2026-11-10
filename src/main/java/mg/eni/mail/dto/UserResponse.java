package mg.eni.mail.dto;
import mg.eni.mail.entity.*;
import java.util.UUID;
public record UserResponse(UUID id, String firstName, String lastName, String email,
                           Role role, boolean active, String department, String promotion,
                           StudyLevel studyLevel, AcademicMention academicMention,
                           AcademicPath academicPath, TeacherPosition teacherPosition,
                           String institutionalResponsibility, String staffPosition, String studentNumber) {}
