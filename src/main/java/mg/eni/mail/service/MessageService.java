package mg.eni.mail.service;

import mg.eni.mail.dto.*;
import mg.eni.mail.entity.*;
import mg.eni.mail.exception.ApiException;
import mg.eni.mail.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class MessageService {
    private final MessageRepository messages;
    private final RecipientRepository recipients;
    private final UserRepository users;
    private final AttachmentRepository attachments;
    private final NotificationService notifications;

    public MessageService(MessageRepository messages, RecipientRepository recipients,
                          UserRepository users, AttachmentRepository attachments, NotificationService notifications) {
        this.messages = messages; this.recipients = recipients; this.users = users; this.attachments = attachments; this.notifications = notifications;
    }


    @Transactional
    public MessageResponse send(SendMessageRequest request, String senderEmail) {
        User sender = findUser(senderEmail);

        Message message = new Message();
        message.setSender(sender);
        message.setSenderEmailSnapshot(sender.getEmail());
        message.setSenderNameSnapshot(sender.getFirstName() + " " + sender.getLastName());
        message.setSubject(request.subject().trim());
        message.setBody(request.body());
        message.setDraft(false);
        message.setSentAt(Instant.now());

        message = messages.save(message);

        Map<String, RecipientType> targets = new LinkedHashMap<>();
        addTargets(targets, request.to(), RecipientType.TO);
        addTargets(targets, request.cc(), RecipientType.CC);
        addTargets(targets, request.bcc(), RecipientType.BCC);

        if (targets.isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Ajoutez au moins un destinataire"
            );
        }

        for (var entry : targets.entrySet()) {
            User recipientUser = findUser(entry.getKey());

            if (recipientUser.getId().equals(sender.getId())) {
                continue;
            }

            Recipient recipient = new Recipient();
            recipient.setMessage(message);
            recipient.setUser(recipientUser);
            recipient.setType(entry.getValue());

            recipients.save(recipient);
            notifications.notifyNewMessage(recipientUser, message);
        }

        return response(message, false, false, false);
    }


    @Transactional
    public MessageResponse saveDraft(SendMessageRequest request, String senderEmail) {
        User sender = findUser(senderEmail);
        Message message = new Message();
        message.setSender(sender);
        message.setSubject(request.subject() == null ? "(Sans objet)" : request.subject().trim());
        message.setBody(request.body() == null ? "" : request.body());
        message.setDraft(true);
        return response(messages.save(message), false, false, false);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> inbox(String email) {
        return recipients.findByUserEmailIgnoreCaseAndDeletedFalseAndArchivedFalseOrderByMessageSentAtDesc(email)
            .stream().filter(r -> !r.getMessage().isDraft()).map(r ->
                response(r.getMessage(), r.isRead(), r.isStarred(), r.isArchived())).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(String email) {
        return recipients.countByUserEmailIgnoreCaseAndReadFalseAndDeletedFalseAndArchivedFalse(email);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> sent(String email) {
        return messages.findBySenderEmailIgnoreCaseAndDraftFalseOrderBySentAtDesc(email)
            .stream().map(m -> response(m, false, false, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> drafts(String email) {
        return messages.findBySenderEmailIgnoreCaseAndDraftTrueOrderByCreatedAtDesc(email)
            .stream().map(m -> response(m, false, false, false)).toList();
    }

    @Transactional
    public MessageResponse get(UUID id, String email) {
        Message m = messages.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Message introuvable"));
        if (!m.getSender().getEmail().equalsIgnoreCase(email) &&
            !recipients.existsByMessageIdAndUserEmailIgnoreCase(id, email))
            throw new ApiException(HttpStatus.FORBIDDEN, "Vous ne pouvez pas consulter ce message");
        Recipient r = recipients.findByMessageIdAndUserEmailIgnoreCase(id, email).orElse(null);
        if (r != null && !m.isDraft()) r.setRead(true);
        if (r != null && !m.isDraft()) notifications.removeForMessage(id, email);
        return response(m, r != null && r.isRead(), r != null && r.isStarred(), r != null && r.isArchived());
    }

    @Transactional
    public void updateRecipientFlag(UUID id, String email, String action) {
        Recipient r = recipients.findByMessageIdAndUserEmailIgnoreCase(id, email)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Message introuvable dans votre boîte"));
        switch (action) {
            case "read" -> { r.setRead(true); notifications.removeForMessage(id, email); }
            case "unread" -> r.setRead(false);
            case "star" -> r.setStarred(!r.isStarred());
            case "archive" -> r.setArchived(true);
            case "restore" -> { r.setArchived(false); r.setDeleted(false); }
            case "delete" -> r.setDeleted(true);
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Action inconnue");
        }
    }


    @Transactional
    public MessageResponse sendDraft(UUID id, SendMessageRequest request, String senderEmail) {
        Message message = messages.findByIdAndSenderEmailIgnoreCaseAndDraftTrue(id, senderEmail)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Brouillon introuvable"));
        Map<String, RecipientType> targets = new LinkedHashMap<>();
        addTargets(targets, request.to(), RecipientType.TO);
        addTargets(targets, request.cc(), RecipientType.CC);
        addTargets(targets, request.bcc(), RecipientType.BCC);
        if (targets.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "Ajoutez au moins un destinataire");
        message.setSubject(request.subject().trim());
        message.setBody(request.body());
        message.setDraft(false);
        message.setSentAt(Instant.now());
        for (var entry : targets.entrySet()) {
            User recipientUser = findUser(entry.getKey());
            if (recipientUser.getId().equals(message.getSender().getId())) continue;
            Recipient recipient = new Recipient();
            recipient.setMessage(message);
            recipient.setUser(recipientUser);
            recipient.setType(entry.getValue());
            recipients.save(recipient);
            notifications.notifyNewMessage(recipientUser, message);
        }
        return response(message, false, false, false);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> archived(String email) {
        return recipients.findByUserEmailIgnoreCaseAndArchivedTrueAndDeletedFalseOrderByMessageSentAtDesc(email)
            .stream().map(r -> response(r.getMessage(), r.isRead(), r.isStarred(), true)).toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> trash(String email) {
        return recipients.findByUserEmailIgnoreCaseAndDeletedTrueOrderByMessageSentAtDesc(email)
            .stream().map(r -> response(r.getMessage(), r.isRead(), r.isStarred(), r.isArchived())).toList();
    }

    @Transactional
    public void deleteDraft(UUID id, String email) {
        Message message = messages.findByIdAndSenderEmailIgnoreCaseAndDraftTrue(id, email)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Brouillon introuvable"));
        messages.delete(message);
    }

    @Transactional
    public void permanentlyDeleteForUser(UUID id, String email) {
        Recipient recipient = recipients.findByMessageIdAndUserEmailIgnoreCase(id, email).orElse(null);
        if (recipient == null || !recipient.isDeleted())
            throw new ApiException(HttpStatus.BAD_REQUEST, "Le message doit d'abord être dans votre corbeille");
        recipients.delete(recipient);
    }

    @Transactional
    public void emptyTrash(String email) {
        recipients.deleteByUserEmailIgnoreCaseAndDeletedTrue(email);
    }

    private User findUser(String email) {
        return users.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable : " + email));
    }

    private void addTargets(Map<String, RecipientType> targets, List<String> emails, RecipientType type) {
        if (emails == null) return;
        for (String email : emails) {
            if (email != null && !email.isBlank()) targets.put(email.trim().toLowerCase(), type);
        }
    }

    private MessageResponse response(Message m, boolean read, boolean starred, boolean archived) {
        List<AttachmentResponse> messageAttachments = attachments.findByMessageId(m.getId()).stream()
            .map(a -> new AttachmentResponse(a.getId(), a.getOriginalName(), a.getContentType(), a.getSize(), a.getUploadedAt())).toList();
        return new MessageResponse(m.getId(), m.getSubject(), m.getBody(),
            m.getSender() == null ? m.getSenderEmailSnapshot() : m.getSender().getEmail(),
            m.getSender() == null ? m.getSenderNameSnapshot() : m.getSender().getFirstName() + " " + m.getSender().getLastName(),
            m.getSentAt(), read, starred, archived, m.isDraft(), messageAttachments);
    }
}
