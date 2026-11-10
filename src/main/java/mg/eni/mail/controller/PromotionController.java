package mg.eni.mail.controller;

import jakarta.validation.Valid;
import mg.eni.mail.dto.PromotionRequest;
import mg.eni.mail.entity.Promotion;
import mg.eni.mail.exception.ApiException;
import mg.eni.mail.repository.PromotionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {
    private final PromotionRepository repository;
    public PromotionController(PromotionRepository repository) { this.repository = repository; }

    @GetMapping
    public List<Promotion> all() { return repository.findAll(); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Promotion create(@Valid @RequestBody PromotionRequest request) {
        if (repository.existsByNameIgnoreCase(request.name()))
            throw new ApiException(HttpStatus.CONFLICT, "Cette promotion existe déjà");
        Promotion p = new Promotion(); p.setName(request.name().trim()); p.setDescription(request.description());
        return repository.save(p);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String,String> delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) throw new ApiException(HttpStatus.NOT_FOUND, "Promotion introuvable");
        repository.deleteById(id); return Map.of("message", "Promotion supprimée");
    }
}
