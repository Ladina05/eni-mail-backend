package mg.eni.mail.controller;

import jakarta.validation.Valid;
import mg.eni.mail.dto.*;
import mg.eni.mail.service.AuthService;
import mg.eni.mail.service.PasswordResetService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final PasswordResetService passwordReset;
    public AuthController(AuthService auth, PasswordResetService passwordReset) { this.auth = auth; this.passwordReset = passwordReset; }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }

    @PostMapping("/forgot-password")
    public java.util.Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return passwordReset.request(request);
    }

    @PostMapping("/validate-reset-token")
    public java.util.Map<String, Boolean> validateResetToken(@Valid @RequestBody ResetTokenRequest request) {
        return passwordReset.validate(request);
    }

    @PostMapping("/reset-password")
    public java.util.Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return passwordReset.reset(request);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) { return auth.me(authentication.getName()); }
}
