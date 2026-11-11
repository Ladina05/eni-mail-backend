package mg.eni.mail.controller;

import jakarta.validation.Valid;
import mg.eni.mail.dto.*;
import mg.eni.mail.service.MessageService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService service;
    public MessageController(MessageService service) { this.service = service; }

    @PostMapping
    public MessageResponse send(@Valid @RequestBody SendMessageRequest request, Authentication auth) {
        return service.send(request, auth.getName());
    }

    @PostMapping("/drafts")
    public MessageResponse draft(@RequestBody SendMessageRequest request, Authentication auth) {
        return service.saveDraft(request, auth.getName());
    }

    @GetMapping("/inbox")
    public List<MessageResponse> inbox(Authentication auth) { return service.inbox(auth.getName()); }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication auth) {
        return Map.of("count", service.unreadCount(auth.getName()));
    }

    @GetMapping("/sent")
    public List<MessageResponse> sent(Authentication auth) { return service.sent(auth.getName()); }

    @GetMapping("/drafts")
    public List<MessageResponse> drafts(Authentication auth) { return service.drafts(auth.getName()); }

    @PostMapping("/drafts/{id}/send")
    public MessageResponse sendDraft(@PathVariable UUID id, @Valid @RequestBody SendMessageRequest request,
                                     Authentication auth) {
        return service.sendDraft(id, request, auth.getName());
    }

    @DeleteMapping("/drafts/{id}")
    public Map<String,String> deleteDraft(@PathVariable UUID id, Authentication auth) {
        service.deleteDraft(id, auth.getName());
        return Map.of("message", "Brouillon supprimé");
    }

    @GetMapping("/archive")
    public List<MessageResponse> archived(Authentication auth) { return service.archived(auth.getName()); }

    @GetMapping("/trash")
    public List<MessageResponse> trash(Authentication auth) { return service.trash(auth.getName()); }

    @GetMapping("/{id}")
    public MessageResponse get(@PathVariable UUID id, Authentication auth) {
        return service.get(id, auth.getName());
    }

    @PatchMapping("/{id}/{action}")
    public Map<String, String> action(@PathVariable UUID id, @PathVariable String action, Authentication auth) {
        service.updateRecipientFlag(id, auth.getName(), action);
        return Map.of("message", "Opération effectuée");
    }

    @DeleteMapping("/{id}/permanent")
    public Map<String, String> permanentlyDelete(@PathVariable UUID id, Authentication auth) {
        service.permanentlyDeleteForUser(id, auth.getName());
        return Map.of("message", "Message supprimé définitivement");
    }

    @DeleteMapping("/trash")
    public Map<String, String> emptyTrash(Authentication auth) {
        service.emptyTrash(auth.getName());
        return Map.of("message", "Corbeille vidée");
    }
}
