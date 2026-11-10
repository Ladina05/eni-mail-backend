package mg.eni.mail.controller;

import jakarta.validation.Valid;
import mg.eni.mail.dto.DepartmentRequest;
import mg.eni.mail.entity.Department;
import mg.eni.mail.exception.ApiException;
import mg.eni.mail.repository.DepartmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentRepository repository;
    public DepartmentController(DepartmentRepository repository) { this.repository = repository; }

    @GetMapping
    public List<Department> all() { return repository.findAll(); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Department create(@Valid @RequestBody DepartmentRequest request) {
        if (repository.existsByNameIgnoreCase(request.name()))
            throw new ApiException(HttpStatus.CONFLICT, "Ce département existe déjà");
        Department d = new Department(); d.setName(request.name().trim()); d.setDescription(request.description());
        return repository.save(d);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String,String> delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) throw new ApiException(HttpStatus.NOT_FOUND, "Département introuvable");
        repository.deleteById(id); return Map.of("message", "Département supprimé");
    }
}
