package mg.eni.mail.controller;

import jakarta.validation.Valid;
import mg.eni.mail.dto.*;
import mg.eni.mail.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }

    @GetMapping
    public List<UserResponse> all(@RequestParam(required=false) String q) {
        return q == null || q.isBlank() ? service.all() : service.search(q);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse create(@Valid @RequestBody UserCreateRequest request) { return service.create(request); }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return service.setActive(id, active);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> delete(@PathVariable UUID id) {
        service.delete(id); return Map.of("message", "Utilisateur supprimé");
    }

    @PatchMapping("/me/password")
    public Map<String, String> changeOwnPassword(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user,
                                                   @Valid @RequestBody PasswordChangeRequest request) {
        service.changeOwnPassword(user.getUsername(), request);
        return Map.of("message", "Mot de passe modifié avec succès");
    }
}
