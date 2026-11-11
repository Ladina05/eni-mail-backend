package mg.eni.mail.controller;

import jakarta.validation.Valid;
import mg.eni.mail.dto.*;
import mg.eni.mail.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.core.io.*;
import org.springframework.web.multipart.MultipartFile;
import mg.eni.mail.entity.User;
import mg.eni.mail.service.FileStorageService;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    private final FileStorageService storage;
    public UserController(UserService service, FileStorageService storage) { this.service = service; this.storage = storage; }

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

    @PostMapping(value = "/me/profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse uploadProfilePhoto(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user,
                                            @RequestParam("file") MultipartFile file) {
        return service.uploadProfilePhoto(user.getUsername(), file);
    }

    @DeleteMapping("/me/profile-photo")
    public UserResponse deleteProfilePhoto(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user) {
        return service.deleteProfilePhoto(user.getUsername());
    }

    @GetMapping("/{id}/profile-photo")
    public ResponseEntity<Resource> profilePhoto(@PathVariable UUID id) {
        User user = service.profilePhoto(id);
        if (user.getProfilePhotoStoredName() == null)
            throw new mg.eni.mail.exception.ApiException(HttpStatus.NOT_FOUND, "Photo de profil introuvable");
        try {
            Resource resource = new UrlResource(storage.load(user.getProfilePhotoStoredName()).toUri());
            if (!resource.exists()) throw new mg.eni.mail.exception.ApiException(HttpStatus.NOT_FOUND, "Photo de profil introuvable");
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(user.getProfilePhotoContentType())).body(resource);
        } catch (java.net.MalformedURLException e) {
            throw new mg.eni.mail.exception.ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible de lire la photo de profil");
        }
    }
}
