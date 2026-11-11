package mg.eni.mail.service;

import mg.eni.mail.dto.NotificationResponse;
import mg.eni.mail.entity.*;
import mg.eni.mail.exception.ApiException;
import mg.eni.mail.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    private final UserRepository users;
    private final SimpMessagingTemplate messaging;

    public NotificationService(NotificationRepository notifications, UserRepository users, SimpMessagingTemplate messaging) {
        this.notifications = notifications; this.users = users; this.messaging = messaging;
    }

    @Transactional
    public void notifyNewMessage(User recipient, Message message) {
        Notification n = new Notification();
        n.setUser(recipient);
        n.setMessage(message);
        n.setTitle("Nouveau message");
        n.setContent("Vous avez reçu un nouveau message : " + message.getSubject());
        notifications.save(n);
        messaging.convertAndSendToUser(recipient.getEmail(), "/queue/notifications", toResponse(n));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(String email) {
        return notifications.findTop50ByUserEmailIgnoreCaseOrderByCreatedAtDesc(email)
            .stream().map(NotificationService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(String email) {
        return notifications.countByUserEmailIgnoreCaseAndReadFalse(email);
    }

    @Transactional
    public void markRead(UUID id, String email) {
        Notification n = notifications.findByIdAndUserEmailIgnoreCase(id, email)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification introuvable"));
        n.setRead(true);
    }

    @Transactional
    public void removeForMessage(UUID messageId, String email) {
        notifications.deleteByMessageIdAndUserEmailIgnoreCase(messageId, email);
    }

    @Transactional
    public void delete(UUID id, String email) {
        Notification n = notifications.findByIdAndUserEmailIgnoreCase(id, email)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification introuvable"));
        notifications.delete(n);
    }

    private static NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getTitle(), n.getContent(), n.isRead(), n.getCreatedAt(), n.getMessage() == null ? null : n.getMessage().getId());
    }
}
