package mg.eni.mail.service;

import mg.eni.mail.dto.*;
import mg.eni.mail.entity.PasswordResetToken;
import mg.eni.mail.entity.User;
import mg.eni.mail.exception.ApiException;
import mg.eni.mail.repository.PasswordResetTokenRepository;
import mg.eni.mail.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;

@Service
public class PasswordResetService {
    private static final long REQUEST_COOLDOWN_SECONDS = 60;
    private static final long TOKEN_LIFETIME_MINUTES = 15;

    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final AccountEmailService emails;

    public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens,
                                PasswordEncoder encoder, AccountEmailService emails) {
        this.users = users; this.tokens = tokens; this.encoder = encoder; this.emails = emails;
    }

    @Transactional
    public Map<String, String> request(ForgotPasswordRequest request) {
        User user = users.findByEmailIgnoreCase(request.email().trim()).orElse(null);
        if (user == null || !user.isActive()) return successMessage();
        Instant now = Instant.now();
        if (tokens.existsByUserIdAndCreatedAtAfter(user.getId(), now.minusSeconds(REQUEST_COOLDOWN_SECONDS)))
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Veuillez attendre une minute avant de demander un nouveau lien.");
        tokens.invalidateUnusedByUserId(user.getId(), now);
        String rawToken = generateToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(now.plus(TOKEN_LIFETIME_MINUTES, ChronoUnit.MINUTES));
        tokens.save(token);
        emails.sendPasswordResetLink(user.getEmail(), user.getFirstName(), rawToken);
        return successMessage();
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> validate(ResetTokenRequest request) {
        return Map.of("valid", findValid(request.token()) != null);
    }

    @Transactional
    public Map<String, String> reset(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmation()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "La confirmation du mot de passe ne correspond pas.");
        PasswordResetToken token = findValid(request.token());
        if (token == null) throw invalidToken();
        Instant now = Instant.now();
        if (tokens.consume(token.getId(), now) != 1) throw invalidToken();
        token.getUser().setPassword(encoder.encode(request.newPassword()));
        tokens.invalidateUnusedByUserId(token.getUser().getId(), now);
        return Map.of("message", "Votre mot de passe a été réinitialisé avec succès.");
    }

    private PasswordResetToken findValid(String rawToken) {
        return tokens.findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(hash(rawToken), Instant.now()).orElse(null);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }

    private ApiException invalidToken() {
        return new ApiException(HttpStatus.BAD_REQUEST, "Ce lien de réinitialisation est invalide, expiré ou déjà utilisé.");
    }

    private Map<String, String> successMessage() {
        return Map.of("message", "Si un compte actif correspond à cette adresse, un lien de réinitialisation a été envoyé.");
    }
}
