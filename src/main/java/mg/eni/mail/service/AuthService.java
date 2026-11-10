
package mg.eni.mail.service;

import mg.eni.mail.dto.*;
import mg.eni.mail.entity.User;
import mg.eni.mail.exception.ApiException;
import mg.eni.mail.repository.UserRepository;
import mg.eni.mail.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.AuthenticationException;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email().trim().toLowerCase(),
                            request.password()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Adresse e-mail ou mot de passe incorrect"
            );
        }

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "Compte introuvable"
                ));

        return new AuthResponse(
                jwtService.generateToken(user.getEmail()),
                toResponse(user)
        );
    }

    @Transactional(readOnly = true)
    public UserResponse me(String email) {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Utilisateur introuvable"
                ));

        return toResponse(user);
    }

    public static UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getDepartment() == null
                        ? null : user.getDepartment().getName(),
                user.getPromotion() == null
                        ? null : user.getPromotion().getName(),
                user.getStudyLevel(),
                user.getAcademicMention(),
                user.getAcademicPath(),
                user.getTeacherPosition(),
                user.getInstitutionalResponsibility(),
                user.getStaffPosition(), user.getStudentNumber()
        );
    }
}
