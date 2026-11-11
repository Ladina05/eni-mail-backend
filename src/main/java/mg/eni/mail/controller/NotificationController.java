package mg.eni.mail.controller;

import mg.eni.mail.dto.NotificationResponse;
import mg.eni.mail.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service = service; }

    @GetMapping
    public List<NotificationResponse> all(Authentication auth) { return service.list(auth.getName()); }

    @GetMapping("/unread-count")
    public Map<String,Long> unreadCount(Authentication auth) {
        return Map.of("count", service.unreadCount(auth.getName()));
    }

    @PatchMapping("/{id}/read")
    public Map<String,String> read(@PathVariable UUID id, Authentication auth) {
        service.markRead(id, auth.getName()); return Map.of("message", "Notification marquée comme lue");
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable UUID id, Authentication auth) {
        service.delete(id, auth.getName()); return Map.of("message", "Notification supprimée");
    }
}
