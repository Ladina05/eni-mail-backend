package mg.eni.mail.service;

import mg.eni.mail.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED = Set.of(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "image/png", "image/jpeg", "text/plain"
    );
    private final Path root;

    public FileStorageService(@Value("${app.storage.location}") String location) {
        this.root = Paths.get(location).toAbsolutePath().normalize();
        try { Files.createDirectories(root); }
        catch (IOException e) { throw new IllegalStateException("Impossible de créer le dossier de stockage", e); }
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "Fichier vide");
        if (file.getSize() > 10L * 1024 * 1024) throw new ApiException(HttpStatus.BAD_REQUEST, "Fichier supérieur à 10 Mo");
        String type = file.getContentType() == null ? "" : file.getContentType();
        if (type.isBlank()) type = contentTypeFromName(file.getOriginalFilename());
        if (!ALLOWED.contains(type)) throw new ApiException(HttpStatus.BAD_REQUEST, "Type de fichier non autorisé");
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
        if (original.contains("..")) throw new ApiException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide");
        String stored = UUID.randomUUID().toString();
        try {
            Files.copy(file.getInputStream(), root.resolve(stored), StandardCopyOption.REPLACE_EXISTING);
            return stored;
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur pendant l'enregistrement du fichier");
        }
    }

    public Path load(String storedName) {
        Path file = root.resolve(storedName).normalize();
        if (!file.startsWith(root)) throw new ApiException(HttpStatus.BAD_REQUEST, "Chemin invalide");
        return file;
    }

    private String contentTypeFromName(String name) {
        if (name == null) return "application/octet-stream";
        String lower = name.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }
}
