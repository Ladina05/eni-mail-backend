package mg.eni.mail.controller;

import mg.eni.mail.entity.*;
import mg.eni.mail.dto.AttachmentResponse;
import mg.eni.mail.exception.ApiException;
import mg.eni.mail.repository.*;
import mg.eni.mail.service.FileStorageService;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@RestController
@RequestMapping("/api/messages/{messageId}/attachments")
public class AttachmentController {
    private final MessageRepository messages;
    private final RecipientRepository recipients;
    private final AttachmentRepository attachments;
    private final FileStorageService storage;

    public AttachmentController(MessageRepository messages, RecipientRepository recipients,
                                 AttachmentRepository attachments, FileStorageService storage) {
        this.messages = messages; this.recipients = recipients; this.attachments = attachments; this.storage = storage;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public AttachmentResponse upload(@PathVariable UUID messageId, @RequestParam("file") MultipartFile file, Authentication auth) {
        Message message = messages.findById(messageId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Message introuvable"));
        boolean isSender = message.getSender() != null
            ? message.getSender().getEmail().equalsIgnoreCase(auth.getName())
            : message.getSenderEmailSnapshot() != null && message.getSenderEmailSnapshot().equalsIgnoreCase(auth.getName());
        if (!isSender)
            throw new ApiException(HttpStatus.FORBIDDEN, "Les pièces jointes ne peuvent être ajoutées qu'à vos brouillons");
        String stored = storage.store(file);
        Attachment a = new Attachment(); a.setMessage(message);
        a.setOriginalName(file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
        a.setStoredName(stored); a.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType()); a.setSize(file.getSize());
        Attachment saved = attachments.save(a);
        return new AttachmentResponse(saved.getId(), saved.getOriginalName(),
            saved.getContentType(), saved.getSize(), saved.getUploadedAt());
    }

    @GetMapping("/{attachmentId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> download(@PathVariable UUID messageId, @PathVariable UUID attachmentId,
                                             Authentication auth) {
        Message message = messages.findById(messageId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Message introuvable"));
        boolean allowed = (message.getSender() != null
            ? message.getSender().getEmail().equalsIgnoreCase(auth.getName())
            : message.getSenderEmailSnapshot() != null && message.getSenderEmailSnapshot().equalsIgnoreCase(auth.getName()))
            || recipients.existsByMessageIdAndUserEmailIgnoreCase(messageId, auth.getName());
        if (!allowed) throw new ApiException(HttpStatus.FORBIDDEN, "Accès refusé");
        Attachment a = attachments.findByIdAndMessageId(attachmentId, messageId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pièce jointe introuvable"));
        try {
            var path = storage.load(a.getStoredName());
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) throw new ApiException(HttpStatus.NOT_FOUND, "Fichier introuvable");
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment().filename(a.getOriginalName()).build().toString())
                .body(resource);
        } catch (java.net.MalformedURLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible de lire le fichier");
        }
    }
}
