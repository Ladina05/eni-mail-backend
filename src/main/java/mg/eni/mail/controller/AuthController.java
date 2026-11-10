package mg.eni.mail.controller;

import jakarta.validation.Valid;
import mg.eni.mail.dto.*;
import mg.eni.mail.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) { return auth.me(authentication.getName()); }
}
