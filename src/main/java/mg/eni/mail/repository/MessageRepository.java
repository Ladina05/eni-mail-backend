package mg.eni.mail.repository;
import mg.eni.mail.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findBySenderEmailIgnoreCaseAndDraftTrueOrderByCreatedAtDesc(String email);
    List<Message> findBySenderEmailIgnoreCaseAndDraftFalseOrderBySentAtDesc(String email);
    Optional<Message> findByIdAndSenderEmailIgnoreCaseAndDraftTrue(UUID id, String email);
    List<Message> findBySenderId(UUID senderId);
}
