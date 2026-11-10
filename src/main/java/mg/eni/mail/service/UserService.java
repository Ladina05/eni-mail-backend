package mg.eni.mail.service;

import mg.eni.mail.dto.*;
import mg.eni.mail.entity.*;
import mg.eni.mail.exception.ApiException;
import mg.eni.mail.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class UserService {
    private final UserRepository users;
    private final DepartmentRepository departments;
    private final PromotionRepository promotions;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, DepartmentRepository departments,
                       PromotionRepository promotions, PasswordEncoder encoder) {
        this.users = users; this.departments = departments; this.promotions = promotions; this.encoder = encoder;
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (users.existsByEmailIgnoreCase(request.email()))
            throw new ApiException(HttpStatus.CONFLICT, "Cette adresse e-mail existe déjà");
        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(encoder.encode(request.password()));
        user.setRole(request.role());
        applyInstitutionalProfile(user, request);
        if (request.departmentId() != null)
            user.setDepartment(departments.findById(request.departmentId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Département introuvable")));
        if (request.promotionId() != null)
            user.setPromotion(promotions.findById(request.promotionId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Promotion introuvable")));
        return AuthService.toResponse(users.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> all() {
        return users.findAll().stream().map(AuthService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> search(String q) {
        String query = q.toLowerCase();
        return users.findAll().stream().filter(user -> user.getFirstName().toLowerCase().contains(query)
            || user.getLastName().toLowerCase().contains(query)
            || (user.getStudentNumber() != null && user.getStudentNumber().toLowerCase().contains(query)))
            .map(AuthService::toResponse).toList();
    }

    @Transactional
    public UserResponse setActive(UUID id, boolean active) {
        User user = users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        user.setActive(active);
        return AuthService.toResponse(users.save(user));
    }

    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest request) {
        User user = users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        String email = request.email().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(email) && users.existsByEmailIgnoreCase(email))
            throw new ApiException(HttpStatus.CONFLICT, "Cette adresse e-mail existe déjà");
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        return AuthService.toResponse(users.save(user));
    }

    @Transactional
    public void delete(UUID id) {
        User user = users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        users.delete(user);
    }

    @Transactional
    public void changeOwnPassword(String email, PasswordChangeRequest request) {
        User user = users.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        if (!encoder.matches(request.currentPassword(), user.getPassword()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Le mot de passe actuel est incorrect");
        if (encoder.matches(request.newPassword(), user.getPassword()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Le nouveau mot de passe doit être différent de l'ancien");
        user.setPassword(encoder.encode(request.newPassword()));
    }

    private void applyInstitutionalProfile(User user, UserCreateRequest request) {
        if (request.role() == Role.STUDENT) {
            if (request.studyLevel() == null || request.academicMention() == null || request.academicPath() == null)
                throw new ApiException(HttpStatus.BAD_REQUEST, "Le niveau, la mention et le parcours sont obligatoires pour un étudiant");
            if (request.academicPath().getMention() != request.academicMention())
                throw new ApiException(HttpStatus.BAD_REQUEST, "Le parcours sélectionné ne correspond pas à la mention");
            if (request.academicMention() == AcademicMention.ARTIFICIAL_INTELLIGENCE
                    && request.studyLevel().ordinal() < StudyLevel.M1.ordinal())
                throw new ApiException(HttpStatus.BAD_REQUEST, "La mention Intelligence artificielle est réservée au niveau Master");
            if ((request.academicPath() == AcademicPath.DATA_GOVERNANCE_ENGINEERING
                    || request.academicPath() == AcademicPath.CONNECTED_OBJECTS_CYBERSECURITY
                    || request.academicPath() == AcademicPath.INFORMATION_SYSTEMS_AUDIT)
                    && request.studyLevel().ordinal() < StudyLevel.M1.ordinal())
                throw new ApiException(HttpStatus.BAD_REQUEST, "Ce parcours est réservé au niveau Master");
            user.setStudyLevel(request.studyLevel());
            user.setAcademicMention(request.academicMention());
            user.setAcademicPath(request.academicPath());
            String studentNumber = trimToNull(request.studentNumber());
            if (studentNumber != null) {
                if (users.existsByStudentNumberIgnoreCase(studentNumber)) throw new ApiException(HttpStatus.CONFLICT, "Ce numéro matricule existe déjà");
                user.setStudentNumber(studentNumber);
            }
        } else if (request.role() == Role.TEACHER) {
            if (request.teacherPosition() == null)
                throw new ApiException(HttpStatus.BAD_REQUEST, "Le poste est obligatoire pour un enseignant");
            user.setTeacherPosition(request.teacherPosition());
            user.setInstitutionalResponsibility(trimToNull(request.institutionalResponsibility()));
        } else if (request.role() == Role.STAFF) {
            if (trimToNull(request.staffPosition()) == null)
                throw new ApiException(HttpStatus.BAD_REQUEST, "La fonction est obligatoire pour le personnel administratif");
            user.setStaffPosition(trimToNull(request.staffPosition()));
        }
    }

    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
